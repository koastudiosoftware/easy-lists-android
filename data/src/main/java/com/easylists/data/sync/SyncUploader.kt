package com.easylists.data.sync

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.SyncDao
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListMemberEntity
import com.easylists.domain.repositories.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.toxicbakery.logging.Arbor
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


// Pushes every row marked dirty in Room up to Firestore.
//
// Order matters because the security rules read the state BEFORE a batch is applied:
//   1. lists, each together with its member rows (the owner row is created with its list)
//   2. categories, tags and list order
//   3. list items
//   4. tag links
// Each batch is committed first, and only then are its rows marked clean in Room.
// A row is only cleared if it hasn't been edited since it was read (see SyncDao.mark...Clean),
// so an edit made during the upload stays dirty and goes up on the next push.
//
// Photos are not uploaded (Cloud Storage needs the paid plan), so photo columns stay local.
class SyncUploader @Inject constructor(
    private val db: EasyListsDatabase,
    private val syncDao: SyncDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val session: SessionRepository,
) {

    // One document to write, and how to mark its Room row clean afterwards
    private class PendingWrite(
        val ref: DocumentReference,
        val data: Map<String, Any?>,
        val markClean: suspend () -> Unit,
    )

    // Uploads everything that is dirty. Returns the number of documents written.
    suspend fun push(): Int {
        val uid = auth.currentUser?.uid ?: return 0
        // The local data belongs to a different account: nothing may go up under this login
        if (session.getUserId() != uid) return 0

        var written = 0
        written += commit(listsWithMembers())
        written += commit(single(categories() + tags() + listOrder()))
        written += commit(single(listItems()))
        written += commit(single(tagLinks()))
        Arbor.i("Sync push finished, $written documents written")
        return written
    }


    //region Batching

    // Each group is kept together in one batch. Groups are packed into batches of at most MAX_BATCH writes.
    private suspend fun commit(groups: List<List<PendingWrite>>): Int {
        var written = 0
        val pending = mutableListOf<PendingWrite>()

        suspend fun flush() {
            if (pending.isEmpty()) return
            val batch = firestore.batch()
            pending.forEach { batch.set(it.ref, it.data + ("synced_at" to FieldValue.serverTimestamp())) }
            batch.commit().await()
            db.withTransaction { pending.forEach { it.markClean() } }
            written += pending.size
            pending.clear()
        }

        for (group in groups) {
            if (pending.size + group.size > MAX_BATCH) flush()
            pending += group
        }
        flush()
        return written
    }

    private fun single(writes: List<PendingWrite>): List<List<PendingWrite>> = writes.map { listOf(it) }

    //endregion


    //region Lists and members

    private suspend fun listsWithMembers(): List<List<PendingWrite>> {
        val membersByList = syncDao.getDirtyMembers().groupBy { it.listId }.toMutableMap()
        val groups = mutableListOf<List<PendingWrite>>()

        syncDao.getDirtyLists().forEach { list ->
            val group = mutableListOf(listWrite(list))
            membersByList.remove(list.listId)?.forEach { group += memberWrite(it) }
            groups += group
        }
        // Member rows whose list itself isn't dirty
        membersByList.values.flatten().forEach { groups += listOf(memberWrite(it)) }
        return groups
    }

    private fun listWrite(l: ListEntity) = PendingWrite(
        ref = firestore.collection("lists").document(l.listId),
        data = mapOf(
            "list_id" to l.listId,
            "owner_id" to l.ownerId,
            "name" to l.name,
            "notes" to l.notes,
            "is_deleted" to l.isDeleted,
            "created_timestamp" to l.createdTimestamp,
            "modified_timestamp" to l.modifiedTimestamp,
        ),
        markClean = { syncDao.markListClean(l.listId, l.modifiedTimestamp) },
    )

    private fun memberWrite(m: ListMemberEntity) = PendingWrite(
        ref = firestore.collection("list_members").document("${m.listId}_${m.userId}"),
        data = mapOf(
            "list_id" to m.listId,
            "user_id" to m.userId,
            "role" to m.role,
            "display_name" to m.displayName,
            "is_deleted" to m.isDeleted,
            "created_timestamp" to m.createdTimestamp,
            "modified_timestamp" to m.modifiedTimestamp,
        ),
        markClean = { syncDao.markMemberClean(m.listId, m.userId, m.modifiedTimestamp) },
    )

    //endregion


    //region Categories, tags and list order

    private suspend fun categories(): List<PendingWrite> = syncDao.getDirtyCategories().map { c ->
        PendingWrite(
            ref = firestore.collection("categories").document(c.categoryId),
            data = mapOf(
                "category_id" to c.categoryId,
                "owner_id" to c.ownerId,
                // Always written, null for a personal category (the rules expect it)
                "list_id" to c.listId,
                "name" to c.name,
                "is_deleted" to c.isDeleted,
                "created_timestamp" to c.createdTimestamp,
                "modified_timestamp" to c.modifiedTimestamp,
            ),
            markClean = { syncDao.markCategoryClean(c.categoryId, c.modifiedTimestamp) },
        )
    }

    private suspend fun tags(): List<PendingWrite> = syncDao.getDirtyTags().map { t ->
        PendingWrite(
            ref = firestore.collection("tags").document(t.tagId),
            data = mapOf(
                "tag_id" to t.tagId,
                "owner_id" to t.ownerId,
                "name" to t.name,
                "color" to t.color,
                "is_deleted" to t.isDeleted,
                "created_timestamp" to t.createdTimestamp,
                "modified_timestamp" to t.modifiedTimestamp,
            ),
            markClean = { syncDao.markTagClean(t.tagId, t.modifiedTimestamp) },
        )
    }

    private suspend fun listOrder(): List<PendingWrite> = syncDao.getDirtyListOrder().map { o ->
        PendingWrite(
            ref = firestore.collection("list_order").document("${o.userId}_${o.listId}"),
            data = mapOf(
                "user_id" to o.userId,
                "list_id" to o.listId,
                "sort_index" to o.sortIndex,
                "created_timestamp" to o.createdTimestamp,
                "modified_timestamp" to o.modifiedTimestamp,
            ),
            markClean = { syncDao.markListOrderClean(o.userId, o.listId, o.modifiedTimestamp) },
        )
    }

    //endregion


    //region List items and tag links

    private suspend fun listItems(): List<PendingWrite> = syncDao.getDirtyListItems().map { i ->
        PendingWrite(
            ref = firestore.collection("list_items").document(i.listItemId),
            data = mapOf(
                "list_item_id" to i.listItemId,
                "list_id" to i.listId,
                "category_id" to i.categoryId,
                "name" to i.name,
                "notes" to i.notes,
                "quantity" to i.quantity,
                "crossed_off" to i.crossedOff,
                "crossed_off_timestamp" to i.crossedOffTimestamp,
                "is_deleted" to i.isDeleted,
                "created_timestamp" to i.createdTimestamp,
                "modified_timestamp" to i.modifiedTimestamp,
            ),
            markClean = { syncDao.markListItemClean(i.listItemId, i.modifiedTimestamp) },
        )
    }

    private suspend fun tagLinks(): List<PendingWrite> = syncDao.getDirtyTagLinks().map { d ->
        val l = d.link
        PendingWrite(
            ref = firestore.collection("tag_list_items").document(l.tagListItemId),
            data = mapOf(
                "tag_list_item_id" to l.tagListItemId,
                "tag_id" to l.tagId,
                "list_item_id" to l.listItemId,
                // The rules need the tag's owner on the document
                "owner_id" to d.tagOwnerId,
                "is_deleted" to l.isDeleted,
                "created_timestamp" to l.createdTimestamp,
                "modified_timestamp" to l.modifiedTimestamp,
            ),
            markClean = { syncDao.markTagLinkClean(l.tagListItemId, l.modifiedTimestamp) },
        )
    }

    //endregion


    private companion object {
        // Firestore allows 500 writes per batch; stay under it
        const val MAX_BATCH = 400
    }
}