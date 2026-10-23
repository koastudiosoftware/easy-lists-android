package com.easylists.data.sync

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.SyncDao
import com.easylists.data.db.room.models.SyncStateEntity
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListMemberEntity
import com.easylists.data.db.room.models.ListOrderEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity
import com.easylists.domain.repositories.ImageRepository
import com.easylists.domain.repositories.SessionRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.toxicbakery.logging.Arbor
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Pulls changes from Firestore into Room.
//
// What it asks the server for, always with an equality filter the rules can check:
//   - my own membership rows            (list_members where user_id == me)
//   - for every list I belong to: the list, its roster, its categories and its items
//   - my private data: tags, tag links, list order and personal categories
// Each query only asks for documents whose synced_at (set by the server on every push) is newer
// than the cursor stored for that query, so nothing is missed however late a device uploads.
//
// Nothing is written to Room until every query has succeeded. Then everything is applied in one
// transaction, parents before children, and the cursors move forward in the same transaction.
//
// Conflicts are decided per row, last writer wins by modified_timestamp:
//   - no local row, or the local row is clean: the server copy replaces it
//   - the local row is dirty: it only loses if the server copy is newer
// Photos never sync, so a pulled item keeps whatever photo this device has.
class SyncDownloader @Inject constructor(
    private val db: EasyListsDatabase,
    private val syncDao: SyncDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val session: SessionRepository,
    private val imageRepository: ImageRepository,
) {

    // Everything fetched during one pull, applied together at the end
    private class Fetched {
        val lists = mutableListOf<ListEntity>()
        val members = mutableListOf<ListMemberEntity>()
        val categories = mutableListOf<CategoryEntity>()
        val items = mutableListOf<ListItemEntity>()
        val tags = mutableListOf<TagEntity>()
        val links = mutableListOf<TagListItemEntity>()
        val order = mutableListOf<ListOrderEntity>()

        // cursor key -> newest synced_at seen, in microseconds
        val cursors = mutableMapOf<String, Long>()
    }

    // Returns the number of rows changed on this device
    suspend fun pull(): Int {
        val uid = auth.currentUser?.uid ?: return 0
        // The local data belongs to a different account: nothing may come in under this login
        if (session.getUserId() != uid) return 0

        val fetched = Fetched()

        // 1. My own membership rows: new lists, role changes, removals
        val myMembers = fetch(
            firestore.collection("list_members").whereEqualTo("user_id", uid),
            "members", fetched,
        ).map { it.toMember() }
        fetched.members += myMembers

        // Lists I was removed from or left (the owner's row is never removed)
        val removedIds = myMembers.filter { it.isDeleted && it.role != OWNER }.map { it.listId }.toSet()

        // 2. The lists to read: my active memberships, with the changes just fetched
        val listIds = (syncDao.getActiveListIds(uid) + myMembers.filter { !it.isDeleted }.map { it.listId })
            .toSet() - removedIds

        // 3. Each list: the list itself, its roster, its categories and its items
        for (listId in listIds) {
            val listDoc = firestore.collection("lists").document(listId).get().await()
            if (listDoc.exists()) fetched.lists += listDoc.toList()

            fetch(firestore.collection("list_members").whereEqualTo("list_id", listId), "roster:$listId", fetched)
                .forEach { fetched.members += it.toMember() }
            fetch(firestore.collection("categories").whereEqualTo("list_id", listId), "categories:$listId", fetched)
                .forEach { fetched.categories += it.toCategory() }
            fetch(firestore.collection("list_items").whereEqualTo("list_id", listId), "items:$listId", fetched)
                .forEach { fetched.items += it.toListItem() }
        }

        // 4. My private data
        fetch(
            firestore.collection("categories").whereEqualTo("owner_id", uid).whereEqualTo("list_id", null),
            "categories_personal", fetched,
        ).forEach { fetched.categories += it.toCategory() }
        fetch(firestore.collection("tags").whereEqualTo("owner_id", uid), "tags", fetched)
            .forEach { fetched.tags += it.toTag() }
        fetch(firestore.collection("tag_list_items").whereEqualTo("owner_id", uid), "tag_links", fetched)
            .forEach { fetched.links += it.toTagLink() }
        fetch(firestore.collection("list_order").whereEqualTo("user_id", uid), "list_order", fetched)
            .forEach { fetched.order += it.toListOrder() }

        // 5. Apply everything, or nothing
        val photoNames = mutableListOf<String>()
        val changed = db.withTransaction {
            var count = 0

            if (removedIds.isNotEmpty()) {
                photoNames += syncDao.getPhotoNames(removedIds.toList())
                syncDao.deleteListsHard(removedIds.toList())
                count += removedIds.size
            }

            count += applyLists(fetched.lists)

            // Parent lists that exist on this device now
            val referencedListIds = fetched.members.map { it.listId } +
                    fetched.categories.mapNotNull { it.listId } +
                    fetched.items.map { it.listId } +
                    fetched.order.map { it.listId }
            val existingLists = byIds(referencedListIds) { syncDao.getExistingListIds(it) }.toSet()

            count += applyMembers(fetched.members.filter { it.listId in existingLists })
            count += applyCategories(fetched.categories.filter { it.listId == null || it.listId in existingLists })
            count += applyListItems(fetched.items.filter { it.listId in existingLists })
            count += applyTags(fetched.tags)
            count += applyTagLinks(fetched.links)
            count += applyListOrder(fetched.order.filter { it.listId in existingLists })

            fetched.cursors.forEach { (key, value) -> syncDao.setCursor(SyncStateEntity(key, value)) }
            syncDao.pruneListCursors()
            count
        }

        imageRepository.deletePhotos(photoNames)
        Arbor.i("Sync pull finished, $changed rows changed")
        return changed
    }


    //region Fetching

    // The documents of a query that are newer than its stored cursor, oldest first
    private suspend fun fetch(query: Query, cursorKey: String, fetched: Fetched): List<DocumentSnapshot> {
        val cursor = syncDao.getCursor(cursorKey) ?: 0L
        val docs = query
            .whereGreaterThan("synced_at", cursor.toTimestamp())
            .orderBy("synced_at")
            .get().await().documents
        docs.mapNotNull { it.getTimestamp("synced_at")?.micros() }.maxOrNull()
            ?.let { fetched.cursors[cursorKey] = it }
        Arbor.i("Sync pull $cursorKey: ${docs.size} new documents")
        return docs
    }

    private fun Timestamp.micros(): Long = seconds * 1_000_000L + nanoseconds / 1_000

    private fun Long.toTimestamp(): Timestamp =
        Timestamp(this / 1_000_000L, ((this % 1_000_000L) * 1_000L).toInt())

    //endregion


    //region Applying

    // The remote rows that should replace what is on this device
    private fun <T> winners(
        remote: List<T>,
        local: Map<String, T>,
        key: (T) -> String,
        dirty: (T) -> Boolean,
        modified: (T) -> Long,
    ): List<T> = remote.filter { r ->
        val l = local[key(r)]
        l == null || if (dirty(l)) modified(r) > modified(l) else modified(r) != modified(l)
    }

    private suspend fun <R> byIds(ids: Collection<String>, query: suspend (List<String>) -> List<R>): List<R> =
        ids.distinct().chunked(ID_CHUNK).flatMap { query(it) }

    private suspend fun applyLists(remote: List<ListEntity>): Int {
        val local = byIds(remote.map { it.listId }) { syncDao.getListsByIds(it) }.associateBy { it.listId }
        val rows = winners(remote, local, { it.listId }, { it.isDirty }, { it.modifiedTimestamp })
        syncDao.upsertLists(rows)
        return rows.size
    }

    private suspend fun applyMembers(remote: List<ListMemberEntity>): Int {
        val key = { m: ListMemberEntity -> "${m.listId}_${m.userId}" }
        val unique = remote.associateBy(key).values.toList()
        val local = byIds(unique.map { it.listId }) { syncDao.getMembersOfLists(it) }.associateBy(key)
        val rows = winners(unique, local, key, { it.isDirty }, { it.modifiedTimestamp })
        syncDao.upsertMembers(rows)
        return rows.size
    }

    private suspend fun applyCategories(remote: List<CategoryEntity>): Int {
        val unique = remote.associateBy { it.categoryId }.values.toList()
        val local = byIds(unique.map { it.categoryId }) { syncDao.getCategoriesByIds(it) }.associateBy { it.categoryId }
        val rows = winners(unique, local, { it.categoryId }, { it.isDirty }, { it.modifiedTimestamp })
        syncDao.upsertCategories(rows)
        return rows.size
    }

    private suspend fun applyListItems(remote: List<ListItemEntity>): Int {
        val unique = remote.associateBy { it.listItemId }.values.toList()
        val local = byIds(unique.map { it.listItemId }) { syncDao.getListItemsByIds(it) }.associateBy { it.listItemId }
        val winning = winners(unique, local, { it.listItemId }, { it.isDirty }, { it.modifiedTimestamp })

        // An item must not point at a category that isn't on this device
        val knownCategories = byIds(winning.mapNotNull { it.categoryId }) { syncDao.getExistingCategoryIds(it) }.toSet()
        val rows = winning.map { r ->
            val withCategory = r.copy(categoryId = r.categoryId?.takeIf { it in knownCategories })
            // Photos stay local: keep this device's photo for an item it already had
            local[r.listItemId]?.let {
                withCategory.copy(
                    photoUri = it.photoUri,
                    photoOffsetX = it.photoOffsetX,
                    photoOffsetY = it.photoOffsetY,
                    photoScale = it.photoScale,
                )
            } ?: withCategory
        }
        syncDao.upsertListItems(rows)
        return rows.size
    }

    private suspend fun applyTags(remote: List<TagEntity>): Int {
        val unique = remote.associateBy { it.tagId }.values.toList()
        val local = byIds(unique.map { it.tagId }) { syncDao.getTagsByIds(it) }.associateBy { it.tagId }
        val rows = winners(unique, local, { it.tagId }, { it.isDirty }, { it.modifiedTimestamp })
        syncDao.upsertTags(rows)
        return rows.size
    }

    private suspend fun applyTagLinks(remote: List<TagListItemEntity>): Int {
        val unique = remote.associateBy { it.tagListItemId }.values.toList()
        // A link needs both its tag and its item on this device
        val tags = byIds(unique.map { it.tagId }) { syncDao.getExistingTagIds(it) }.toSet()
        val items = byIds(unique.map { it.listItemId }) { syncDao.getExistingListItemIds(it) }.toSet()
        val usable = unique.filter { it.tagId in tags && it.listItemId in items }
        val local = byIds(usable.map { it.tagListItemId }) { syncDao.getTagLinksByIds(it) }.associateBy { it.tagListItemId }
        val rows = winners(usable, local, { it.tagListItemId }, { it.isDirty }, { it.modifiedTimestamp })
        syncDao.upsertTagLinks(rows)
        return rows.size
    }

    private suspend fun applyListOrder(remote: List<ListOrderEntity>): Int {
        val userId = remote.firstOrNull()?.userId ?: return 0
        val key = { o: ListOrderEntity -> "${o.userId}_${o.listId}" }
        val unique = remote.associateBy(key).values.toList()
        val local = byIds(unique.map { it.listId }) { syncDao.getListOrderByLists(userId, it) }.associateBy(key)
        val rows = winners(unique, local, key, { it.isDirty }, { it.modifiedTimestamp })
        syncDao.upsertListOrder(rows)
        return rows.size
    }

    //endregion


    //region Documents to entities

    private fun DocumentSnapshot.toList() = ListEntity(
        listId = id,
        ownerId = getString("owner_id").orEmpty(),
        name = getString("name").orEmpty(),
        notes = getString("notes"),
        isDirty = false,
        isDeleted = getBoolean("is_deleted") ?: false,
        isShared = getBoolean("is_shared") ?: false,
        createdTimestamp = getLong("created_timestamp") ?: 0L,
        modifiedTimestamp = getLong("modified_timestamp") ?: 0L,
    )

    private fun DocumentSnapshot.toMember() = ListMemberEntity(
        listId = getString("list_id").orEmpty(),
        userId = getString("user_id").orEmpty(),
        role = getString("role").orEmpty(),
        displayName = getString("display_name"),
        isDirty = false,
        isDeleted = getBoolean("is_deleted") ?: false,
        createdTimestamp = getLong("created_timestamp") ?: 0L,
        modifiedTimestamp = getLong("modified_timestamp") ?: 0L,
    )

    private fun DocumentSnapshot.toCategory() = CategoryEntity(
        categoryId = id,
        ownerId = getString("owner_id").orEmpty(),
        name = getString("name").orEmpty(),
        isDirty = false,
        isDeleted = getBoolean("is_deleted") ?: false,
        createdTimestamp = getLong("created_timestamp") ?: 0L,
        modifiedTimestamp = getLong("modified_timestamp") ?: 0L,
        listId = getString("list_id"),
    )

    private fun DocumentSnapshot.toListItem() = ListItemEntity(
        listItemId = id,
        listId = getString("list_id").orEmpty(),
        categoryId = getString("category_id"),
        name = getString("name").orEmpty(),
        notes = getString("notes"),
        quantity = getLong("quantity")?.toInt(),
        crossedOff = getBoolean("crossed_off") ?: false,
        crossedOffTimestamp = getLong("crossed_off_timestamp"),
        isDirty = false,
        isDeleted = getBoolean("is_deleted") ?: false,
        createdTimestamp = getLong("created_timestamp") ?: 0L,
        modifiedTimestamp = getLong("modified_timestamp") ?: 0L,
    )

    private fun DocumentSnapshot.toTag() = TagEntity(
        tagId = id,
        ownerId = getString("owner_id").orEmpty(),
        name = getString("name").orEmpty(),
        color = getString("color"),
        isDirty = false,
        isDeleted = getBoolean("is_deleted") ?: false,
        createdTimestamp = getLong("created_timestamp") ?: 0L,
        modifiedTimestamp = getLong("modified_timestamp") ?: 0L,
    )

    private fun DocumentSnapshot.toTagLink() = TagListItemEntity(
        tagListItemId = id,
        tagId = getString("tag_id").orEmpty(),
        listItemId = getString("list_item_id").orEmpty(),
        isDirty = false,
        isDeleted = getBoolean("is_deleted") ?: false,
        createdTimestamp = getLong("created_timestamp") ?: 0L,
        modifiedTimestamp = getLong("modified_timestamp") ?: 0L,
    )

    private fun DocumentSnapshot.toListOrder() = ListOrderEntity(
        userId = getString("user_id").orEmpty(),
        listId = getString("list_id").orEmpty(),
        sortIndex = getLong("sort_index")?.toInt() ?: 0,
        isDirty = false,
        createdTimestamp = getLong("created_timestamp") ?: 0L,
        modifiedTimestamp = getLong("modified_timestamp") ?: 0L,
    )

    //endregion


    private companion object {
        const val OWNER = "owner"

        // Stay well under SQLite's limit on the number of parameters in one query
        const val ID_CHUNK = 500
    }
}