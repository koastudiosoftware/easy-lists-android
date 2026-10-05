package com.easylists.domain.repositories

import com.easylists.domain.common.Themes
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun getBooleanAppSetting(key: String): String
    suspend fun getLongAppSetting(key: String): Long
    suspend fun getStringAppSetting(key: String): String

    suspend fun getStringAppSettingTheme(key: String): Flow<Themes>

    suspend fun setBooleanAppSetting(key: String, value: Boolean)
    suspend fun setLongAppSetting(key: String, value: Long)
    suspend fun setStringAppSetting(key: String, value: String)

    suspend fun removeAppSetting(key: String, type: String)

    // New: emits the stored value as a string, "" when unset
    fun getAppSettingFlow(key: String, type: String): Flow<String>
}
