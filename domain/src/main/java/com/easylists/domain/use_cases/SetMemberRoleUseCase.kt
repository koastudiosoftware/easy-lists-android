package com.easylists.domain.use_cases

import com.easylists.domain.models.InviteRole
import com.easylists.domain.repositories.MembersRepository
import javax.inject.Inject

class SetMemberRoleUseCase @Inject constructor(
    private val repo: MembersRepository,
) {
    suspend operator fun invoke(listId: String, userId: String, role: InviteRole) =
        repo.setRole(listId, userId, role)
}
