package com.easylists.domain.use_cases

import com.easylists.domain.models.InviteRole
import com.easylists.domain.repositories.InviteRepository
import javax.inject.Inject

class CreateInviteUseCase @Inject constructor(
    private val repo: InviteRepository
) {

    suspend operator fun invoke(
        listId: String,
        role: InviteRole,
        email: String? = null,
        token: String? = null
    ) = repo.createInvite(listId, role, email, token)

}
