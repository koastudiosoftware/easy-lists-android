package com.easylists.domain.repositories

import com.easylists.domain.common.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
    // keep the old generic functions until everything has migrated
}
