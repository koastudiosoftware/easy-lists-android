package com.easylists.domain.use_cases

import com.easylists.domain.models.AuthAccount
import com.easylists.domain.models.LinkResult
import com.easylists.domain.repositories.SessionRepository
import javax.inject.Inject

class LinkAccountUseCase @Inject constructor(
    private val repo: SessionRepository
) {

    suspend operator fun invoke(account: AuthAccount): LinkResult = repo.linkAccount(account)

}
