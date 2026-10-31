package com.easylists.domain.repositories

import com.easylists.domain.models.ClaimResult
import com.easylists.domain.models.Invite
import com.easylists.domain.models.InvitePreview
import com.easylists.domain.models.InviteRole
import com.easylists.domain.models.JoinedList

interface InviteRepository {

    // TODO: token is only for testing; normally it is generated
    suspend fun createInvite(
        listId: String,
        role: InviteRole,
        email: String? = null,
        token: String? = null
    ): Invite

    suspend fun claimInvite(token: String): ClaimResult

    // reads an invitation without using it up; null if it doesn't exist
    suspend fun previewInvite(token: String): InvitePreview?

    // The list this person already joined with this invite, if any. A used-up
    // invite can still be one's own.
    suspend fun findJoinedList(token: String): JoinedList?

}
