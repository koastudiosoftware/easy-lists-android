package com.easylists.domain.repositories

import com.easylists.domain.models.SyncSummary

interface SyncRepository {
    suspend fun sync(): SyncSummary
}
