package com.easylists.domain.use_cases

import com.easylists.domain.common.AppSettingsType
import com.easylists.domain.repositories.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetAppSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun <T> invoke(
        key: String,
        value: T,
        type: String = AppSettingsType.String.toString()
    ) {

        withContext(Dispatchers.IO) {
            when (type) {
                AppSettingsType.Boolean.toString() -> settingsRepository.setBooleanAppSetting(
                    key = key,
                    value = value as Boolean
                )

                AppSettingsType.Long.toString() -> settingsRepository.setLongAppSetting(
                    key = key,
                    value = value as Long
                )

                else -> settingsRepository.setStringAppSetting(key = key, value = value.toString())
            }
        }
    }

}
