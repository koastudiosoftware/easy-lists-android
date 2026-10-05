package com.easylists.domain.use_cases

import com.easylists.domain.common.AppSettingsType
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.repositories.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(keys: List<Map<String, String>>): List<Map<String, String>> {
        val values = mutableListOf<Map<String,String>>()

        withContext(Dispatchers.IO) {
            for (key in keys) {
                val keyName = key[KEY].toString()
                val type = key[TYPE].toString()

                when (type) {
                    AppSettingsType.Boolean.toString() -> {
                        val value = settingsRepository.getBooleanAppSetting(key = keyName)
                        if (value.isNotEmpty()) {
                            values.add(mapOf(KEY to keyName, VALUE to value))
                        }
                    }
                    AppSettingsType.Long.toString() -> {
                        val value = settingsRepository.getLongAppSetting(key = keyName).toString()
                        if (value.isNotEmpty()) {
                            values.add(mapOf(KEY to keyName, VALUE to value))
                        }
                    }
                    else -> {
                        val value = settingsRepository.getStringAppSetting(key = keyName)
                        if (value.isNotEmpty()) {
                            values.add(mapOf(KEY to keyName, VALUE to value))
                        }
                    }
                }
            }
        }

        return values
    }

}