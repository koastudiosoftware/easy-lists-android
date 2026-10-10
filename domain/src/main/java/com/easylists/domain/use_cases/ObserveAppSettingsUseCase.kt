package com.easylists.domain.use_cases

import com.easylists.domain.common.AppSettings
import com.easylists.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppSettingsUseCase @Inject constructor(
    private val repo: SettingsRepository
) {

    operator fun invoke(): Flow<AppSettings> = repo.settings

}
