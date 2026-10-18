package com.easylists.domain.repositories

import com.easylists.domain.models.AuthAccount
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAccount(): Flow<AuthAccount?>
    suspend fun signInWithGoogle(idToken: String): AuthAccount
    fun signOut()
}
