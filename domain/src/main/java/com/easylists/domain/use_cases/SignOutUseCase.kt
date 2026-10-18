package com.easylists.domain.use_cases

import com.easylists.domain.repositories.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    operator fun invoke() = repo.signOut()
}
