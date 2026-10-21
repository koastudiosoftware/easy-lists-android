package com.easylists.domain.repositories

import com.easylists.domain.models.SyncSummary

interface SyncRepository {

    // minIntervalSeconds > 0 skips the sync when one finished that recently
    suspend fun sync(minIntervalSeconds: Long = 0): SyncSummary

}
