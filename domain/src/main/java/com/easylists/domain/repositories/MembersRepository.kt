package com.easylists.domain.repositories

import com.easylists.domain.models.InviteRole
import com.easylists.domain.models.ListMember
import com.easylists.domain.models.PendingInvite
import kotlinx.coroutines.flow.Flow

interface MembersRepository {

    // The people on the list, the owner first. Works offline.
    fun observeMembers(listId: String): Flow<List<ListMember>>

    // Owner only. Saved on the device first and sent by the next sync.
    suspend fun setRole(listId: String, userId: String, role: InviteRole)

    // Owner only. The removed person's phone drops the list on its next sync.
    suspend fun removeMember(listId: String, userId: String)

    // A collaborator leaves. The server is told first, so this needs a
    // connection; only then is the list removed from this device.
    suspend fun leaveList(listId: String)

    // Owner only; reads the server
    suspend fun pendingInvites(listId: String): List<PendingInvite>

    // Owner only; deletes on the server
    suspend fun revokeInvite(token: String)
}
