package com.easylists.domain.repositories

import com.easylists.domain.models.ClaimResult
import com.easylists.domain.models.Invite
import com.easylists.domain.models.InvitePreview
import com.easylists.domain.models.InviteRole

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

}
