package com.easylists.data.repositories

import android.util.Base64
import com.easylists.domain.models.ClaimResult
import com.easylists.domain.models.Invite
import com.easylists.domain.models.InviteRole
import com.easylists.domain.repositories.InviteRepository
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.repositories.ShareRepository
import com.easylists.domain.repositories.SyncRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.toxicbakery.logging.Arbor
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.sql.Date
import javax.inject.Inject

// Creating and claiming invites. Invites exist only on the server (invites/{token}); the token is
// the document ID and the only secret, so it is long and random.
//
// The security rules do the real checking. The checks here only give friendlier answers:
//   - creating: only the list owner, a role of editor or viewer, an expiry in the future
//   - claiming: the invite must exist, match the list and role being claimed, be unexpired, match
//     the signed-in verified email when it is email-bound, and be deleted in the same batch as the
//     member row is written, which is what makes it single use
class InviteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val session: SessionRepository,
    private val shareRepository: ShareRepository,
    private val syncRepository: SyncRepository,
) : InviteRepository {

    override suspend fun createInvite(
        listId: String,
        role: InviteRole,
        email: String?,
        token: String?,
    ): Invite {
        val uid = auth.currentUser?.uid ?: error("Not signed in")

        // The rules only let the owner invite, and they look the owner up on the server, so the
        // list, its owner row and its categories have to be there before the invite is written.
        shareRepository.prepareListForSharing(listId)
        syncRepository.sync()

        val inviteToken = token ?: newToken()
        val now = System.currentTimeMillis()
        val expiresAt = now + INVITE_LIFETIME_MS

        val data = mutableMapOf<String, Any?>(
            "list_id" to listId,
            "role" to role.id,
            "created_by" to uid,
            "expires_at" to Timestamp(Date(expiresAt)),
            "created_timestamp" to now,
        )
        // Stored lowercase: the rules compare it with the signed-in email, ignoring case
        email?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }?.let { data["email"] = it }

        firestore.collection("invites").document(inviteToken).set(data).await()
        Arbor.i("Invite created for list $listId as ${role.id}")
        return Invite(token = inviteToken, listId = listId, role = role, expiresAtMillis = expiresAt)
    }

    override suspend fun claimInvite(token: String): ClaimResult {
        val user = auth.currentUser ?: return ClaimResult.NotSignedIn
        // The local data has to belong to this account, or the joined list couldn't be pulled in
        if (session.getUserId() != user.uid) return ClaimResult.NotSignedIn

        val inviteRef = firestore.collection("invites").document(token)
        val invite = inviteRef.get().await()
        if (!invite.exists()) return ClaimResult.NotFound

        val listId = invite.getString("list_id") ?: return ClaimResult.NotFound
        val role = invite.getString("role") ?: return ClaimResult.NotFound

        val expiresAt = invite.getTimestamp("expires_at")?.toDate()?.time ?: 0L
        if (expiresAt <= System.currentTimeMillis()) return ClaimResult.Expired

        val boundEmail = invite.getString("email")
        if (boundEmail != null) {
            val mine = user.email?.lowercase()
            if (mine != boundEmail || !user.isEmailVerified) return ClaimResult.WrongEmail
        }

        // Do I already have a member row for this list? A query, because the rules refuse to read
        // a document that doesn't exist.
        val existing = firestore.collection("list_members")
            .whereEqualTo("user_id", user.uid)
            .whereEqualTo("list_id", listId)
            .limit(1)
            .get().await()
            .documents.firstOrNull()
        if (existing != null && existing.getBoolean("is_deleted") == false) return ClaimResult.AlreadyMember

        val now = System.currentTimeMillis()
        val memberRef = firestore.collection("list_members").document("${listId}_${user.uid}")
        val batch = firestore.batch()
        if (existing == null) {
            batch.set(
                memberRef,
                mapOf(
                    "list_id" to listId,
                    "user_id" to user.uid,
                    "role" to role,
                    "display_name" to user.displayName,
                    "is_deleted" to false,
                    "invite_token" to token,
                    "created_timestamp" to now,
                    "modified_timestamp" to now,
                    "synced_at" to FieldValue.serverTimestamp(),
                )
            )
        } else {
            // Rejoining after leaving or being removed: only these fields may change
            batch.update(
                memberRef,
                mapOf(
                    "is_deleted" to false,
                    "role" to role,
                    "invite_token" to token,
                    "modified_timestamp" to now,
                    "synced_at" to FieldValue.serverTimestamp(),
                )
            )
        }
        // Consuming the invite in the same batch is what the rules require
        batch.delete(inviteRef)

        try {
            batch.commit().await()
        } catch (e: FirebaseFirestoreException) {
            // Used, revoked or changed by someone else since it was read
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) return ClaimResult.NotFound
            throw e
        }

        // Pull the list in
        syncRepository.sync()
        return ClaimResult.Joined(listId)
    }

    // 128 random bits, URL-safe, so the token can't be guessed
    private fun newToken(): String {
        val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private companion object {
        // A week
        const val INVITE_LIFETIME_MS = 7L * 24 * 60 * 60 * 1000
    }
}