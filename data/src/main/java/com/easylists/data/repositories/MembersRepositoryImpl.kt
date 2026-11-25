package com.easylists.data.repositories

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.ListMemberDao
import com.easylists.data.db.room.dao.SyncDao
import com.easylists.data.sync.retryOnUnavailable
import com.easylists.domain.models.InviteRole
import com.easylists.domain.models.ListMember
import com.easylists.domain.models.ListRole
import com.easylists.domain.models.PendingInvite
import com.easylists.domain.models.SyncScheduler
import com.easylists.domain.repositories.ImageRepository
import com.easylists.domain.repositories.MembersRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Managing who is on a list.
//
// Role changes and removals are saved in Room first and sent by the next sync;
// the security rules only let the owner make them. Leaving and revoking talk
// to the server directly, because they must not be undone by the list
// disappearing from the device, and so they need a connection.
class MembersRepositoryImpl @Inject constructor(
    private val db: EasyListsDatabase,
    private val memberDao: ListMemberDao,
    private val syncDao: SyncDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val imageRepository: ImageRepository,
    private val scheduler: SyncScheduler,
) : MembersRepository {

    override fun observeMembers(listId: String): Flow<List<ListMember>> {
        return memberDao.observeRoster(listId).map { rows ->
            rows.map { row ->
                ListMember(
                    userId = row.userId,
                    displayName = row.displayName,
                    role = when (row.role) {
                        "owner" -> ListRole.Owner
                        "editor" -> ListRole.Editor
                        else -> ListRole.Viewer
                    },
                    isYou = row.isYou,
                )
            }
        }
    }

    override suspend fun setRole(listId: String, userId: String, role: InviteRole) {
        memberDao.setRole(listId, userId, role.id, now())
        // Send it now, not after the usual wait
        scheduler.requestSync()
    }

    override suspend fun removeMember(listId: String, userId: String) {
        memberDao.removeMember(listId, userId, now())
        scheduler.requestSync()
    }

    override suspend fun leaveList(listId: String) {
        val uid = auth.currentUser?.uid ?: error("Not signed in")

        // The server first. If this failed after the list was gone from the
        // device, the person would still be a member with no way to see that.
        retryOnUnavailable {
            firestore.collection("list_members")
                .document("${listId}_$uid")
                .update(
                    mapOf(
                        "is_deleted" to true,
                        "left" to true,
                        "modified_timestamp" to now(),
                        "synced_at" to FieldValue.serverTimestamp(),
                    )
                )
                .await()
        }

        // Then the list leaves this device, with its items, categories and photos
        val photoNames = syncDao.getPhotoNames(listOf(listId))
        db.withTransaction {
            syncDao.deleteListsHard(listOf(listId))
            syncDao.pruneListCursors()
        }
        imageRepository.deletePhotos(photoNames)
    }

    override suspend fun pendingInvites(listId: String): List<PendingInvite> {
        val snapshot = retryOnUnavailable {
            firestore.collection("invites")
                .whereEqualTo("list_id", listId)
                .get().await()
        }
        val now = System.currentTimeMillis()
        return snapshot.documents.mapNotNull { doc ->
            val expiresAt = doc.getTimestamp("expires_at")?.toDate()?.time
                ?: return@mapNotNull null
            // An expired invite does nothing, so there is nothing to revoke
            if (expiresAt <= now) return@mapNotNull null
            PendingInvite(
                token = doc.id,
                role = if (doc.getString("role") == "viewer") {
                    InviteRole.Viewer
                } else {
                    InviteRole.Editor
                },
                expiresAtMillis = expiresAt,
                email = doc.getString("email"),
            )
        }.sortedBy { it.expiresAtMillis }
    }

    override suspend fun revokeInvite(token: String) {
        retryOnUnavailable {
            firestore.collection("invites").document(token).delete().await()
        }
    }

    private fun now(): Long = System.currentTimeMillis()
}