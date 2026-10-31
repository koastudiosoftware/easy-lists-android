package com.easylists.domain.use_cases

import com.easylists.domain.repositories.MembersRepository
import javax.inject.Inject

class RevokeInviteUseCase @Inject constructor(
    private val repo: MembersRepository,
) {
    suspend operator fun invoke(token: String) = repo.revokeInvite(token)
}
