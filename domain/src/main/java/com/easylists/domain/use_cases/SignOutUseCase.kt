package com.easylists.domain.use_cases

import com.easylists.domain.repositories.AuthRepository
import com.easylists.domain.repositories.SessionRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke() {
        session.removeSharedListsFromDevice()
        auth.signOut()
    }
}
