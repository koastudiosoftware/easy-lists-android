package com.easylists.domain.use_cases

import com.easylists.domain.models.PendingInvite
import com.easylists.domain.repositories.MembersRepository
import javax.inject.Inject

class GetPendingInvitesUseCase @Inject constructor(
    private val repo: MembersRepository,
) {
    suspend operator fun invoke(listId: String): List<PendingInvite> =
        repo.pendingInvites(listId)
}
