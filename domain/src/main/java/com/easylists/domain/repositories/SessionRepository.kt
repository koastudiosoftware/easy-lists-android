package com.easylists.domain.repositories

interface SessionRepository {
    suspend fun getUserId(): String
}
