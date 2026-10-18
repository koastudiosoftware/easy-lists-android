package com.easylists.domain.use_cases

import com.easylists.domain.models.AuthAccount
import com.easylists.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthAccountUseCase @Inject constructor(
    private val repo: AuthRepository
) {

    operator fun invoke(): Flow<AuthAccount?> = repo.observeAccount()

}
