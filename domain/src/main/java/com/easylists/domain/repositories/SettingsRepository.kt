package com.easylists.domain.repositories

interface SettingsRepository {

    suspend fun getBooleanAppSetting(key: String): String
    suspend fun getLongAppSetting(key: String): Long
    suspend fun getStringAppSetting(key: String): String

    suspend fun setBooleanAppSetting(key: String, value: Boolean)
    suspend fun setLongAppSetting(key: String, value: Long)
    suspend fun setStringAppSetting(key: String, value: String)

    suspend fun removeAppSetting(key: String, type: String)

}