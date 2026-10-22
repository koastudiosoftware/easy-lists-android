package com.easylists.data.repositories

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.ShareDao
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.repositories.ShareRepository
import javax.inject.Inject
import kotlin.time.Clock

// Sharing a list.
//
// Step one: the categories its items use become the list's own. Each personal category an item
// uses is copied once into a category that belongs to the list (a new ID, the same name), and the
// list's items are repointed at the copy. From then on the owner and every collaborator use the
// same category rows, so a rename reaches everyone. The owner's personal categories are untouched.
//
// It is safe to run again: items that already use the list's own categories are skipped.
class ShareRepositoryImpl @Inject constructor(
    private val db: EasyListsDatabase,
    private val shareDao: ShareDao,
    private val session: SessionRepository,
) : ShareRepository {

    override suspend fun prepareListForSharing(listId: String) {
        // Read before the transaction opens: collecting a Room flow inside one can stall
        val me = session.getUserId()
        val now = Clock.System.now().toEpochMilliseconds()

        db.withTransaction {
            // Only the owner can share a list
            val owner = shareDao.getOwnerId(listId) ?: error("List $listId is not on this device")
            check(owner == me) { "Only the owner can share a list" }

            shareDao.getPersonalCategoriesUsedBy(listId).forEach { personal ->
                val copy = CategoryEntity(
                    ownerId = me,
                    name = personal.name,
                    isDirty = true,
                    createdTimestamp = now,
                    modifiedTimestamp = now,
                    listId = listId,
                )
                shareDao.insertCategory(copy)
                shareDao.repointItems(listId, from = personal.categoryId, to = copy.categoryId, now = now)
            }
        }
    }

    // Debug rows only: the first list the signed-in user owns
    override suspend fun firstOwnedListId(): String? = shareDao.getFirstOwnedListId(session.getUserId())
}