package com.easylists.domain.use_cases

import com.easylists.domain.models.AuthAccount
import com.easylists.domain.repositories.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthAccount = repo.signInWithGoogle(idToken)
}
