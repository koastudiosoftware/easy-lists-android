package com.easylists.domain.use_cases

import com.easylists.domain.models.ClaimResult
import com.easylists.domain.repositories.InviteRepository
import javax.inject.Inject

class ClaimInviteUseCase @Inject constructor(
    private val repo: InviteRepository
) {

    suspend operator fun invoke(token: String): ClaimResult = repo.claimInvite(token)

}
