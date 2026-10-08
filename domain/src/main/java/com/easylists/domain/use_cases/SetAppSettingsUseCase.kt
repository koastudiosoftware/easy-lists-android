package com.easylists.domain.use_cases

import com.easylists.domain.common.AppSettings
import com.easylists.domain.repositories.SettingsRepository
import javax.inject.Inject

class UpdateAppSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(transform: (AppSettings) -> AppSettings) = repo.update(transform)
}
