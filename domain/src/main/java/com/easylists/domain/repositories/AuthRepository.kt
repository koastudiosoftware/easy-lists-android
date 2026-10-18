package com.easylists.domain.repositories

import com.easylists.domain.models.AuthAccount

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): AuthAccount
}
