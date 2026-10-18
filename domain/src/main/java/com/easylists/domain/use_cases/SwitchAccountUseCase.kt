package com.easylists.domain.use_cases

import com.easylists.domain.models.AuthAccount
import com.easylists.domain.repositories.SessionRepository
import javax.inject.Inject

class SwitchAccountUseCase @Inject constructor(
    private val repo: SessionRepository
) {

    suspend operator fun invoke(account: AuthAccount) {
        repo.switchAccount(account)
    }

}
