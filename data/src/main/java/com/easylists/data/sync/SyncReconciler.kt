package com.easylists.data.sync

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.ReconcileDao
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.domain.repositories.SessionRepository
import com.toxicbakery.logging.Arbor
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Clock

// Merges duplicate personal categories and tags by name. Runs after a pull and before a push.
//
// Two cases make duplicates: a local-only user signing in to an account that already has data,
// and the same name being created on two devices before they synced.
//
// Rules:
//   - Names are compared with whitespace trimmed and case ignored.
//   - The survivor is a copy the server already has (a clean row) over one that is new on this
//     device (a dirty row), then the oldest, then the smallest ID, so every device picks the same one.
//   - Items (or tag links) that pointed at a duplicate are repointed at the survivor.
//   - The duplicate itself is soft-deleted, so the deletion reaches the other devices.
//   - Only personal categories are merged. A category that belongs to a shared list is never touched.
//   - Lists are never merged.
//
// It is safe to run on every sync: with no duplicates it changes nothing.
class SyncReconciler @Inject constructor(
    private val db: EasyListsDatabase,
    private val dao: ReconcileDao,
    private val session: SessionRepository,
) {

    // Returns the number of rows changed
    suspend fun reconcile(): Int {
        // Read before the transaction opens: collecting a Room flow inside one can stall
        val ownerId = session.getUserId()
        val now = Clock.System.now().toEpochMilliseconds()
        val changed = db.withTransaction {
            reconcileCategories(ownerId, now) + reconcileTags(ownerId, now)
        }
        if (changed > 0) Arbor.i("Sync reconcile finished, $changed rows changed")
        return changed
    }

    private suspend fun reconcileCategories(ownerId: String, now: Long): Int {
        var changed = 0
        val order = compareBy<CategoryEntity>({ it.isDirty }, { it.createdTimestamp }, { it.categoryId })

        dao.getPersonalCategories(ownerId)
            .groupBy { normalize(it.name) }
            .values
            .filter { it.size > 1 }
            .forEach { group ->
                // No live copy in the group means there is nothing to merge into
                val survivor = group.filter { !it.isDeleted }.minWithOrNull(order) ?: return@forEach

                group.filter { it.categoryId != survivor.categoryId }.forEach { duplicate ->
                    changed += dao.repointItems(duplicate.categoryId, survivor.categoryId, now)
                    if (!duplicate.isDeleted) {
                        changed += dao.softDeleteCategory(duplicate.categoryId, now)
                    }
                }
            }
        return changed
    }

    private suspend fun reconcileTags(ownerId: String, now: Long): Int {
        var changed = 0
        val order = compareBy<TagEntity>({ it.isDirty }, { it.createdTimestamp }, { it.tagId })

        dao.getTagsOf(ownerId)
            .groupBy { normalize(it.name) }
            .values
            .filter { it.size > 1 }
            .forEach { group ->
                val survivor = group.filter { !it.isDeleted }.minWithOrNull(order) ?: return@forEach

                // Items the survivor already tags, so an item never ends up with the same tag twice
                val alreadyTagged = dao.getLinkedItemIds(survivor.tagId).toMutableSet()

                group.filter { it.tagId != survivor.tagId }.forEach { duplicate ->
                    dao.getLiveLinksOfTag(duplicate.tagId).forEach { link ->
                        changed += if (alreadyTagged.add(link.listItemId)) {
                            dao.retargetLink(link.tagListItemId, survivor.tagId, now)
                        } else {
                            dao.softDeleteLink(link.tagListItemId, now)
                        }
                    }
                    if (!duplicate.isDeleted) {
                        changed += dao.softDeleteTag(duplicate.tagId, now)
                    }
                }
            }
        return changed
    }

    private fun normalize(name: String): String = name.trim().lowercase(Locale.ROOT)
}
