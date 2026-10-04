package com.easylists.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.easylists.domain.common.AppSettingsType
import com.easylists.domain.common.Themes
import com.easylists.domain.repositories.SettingsRepository
import com.toxicbakery.logging.Arbor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppSettingsRepositoryImpl @Inject constructor(
    private val appSettingsDataStore: DataStore<Preferences>
) : SettingsRepository {


    //region getBooleanAppSetting()
    override suspend fun getBooleanAppSetting(key: String): String {
        return try {
            if (appSettingsDataStore.data.first()[booleanPreferencesKey(key)] == null)
                return "null"

            return if (appSettingsDataStore.data.first()[booleanPreferencesKey(key)] == true)
                "true" else "false"
        } catch (e: Exception) {
            Arbor.e("getBooleanAppSetting ERROR: $e")
            false
        }.toString()
    }
    //endregion


    //region getLongAppSetting()
    override suspend fun getLongAppSetting(key: String): Long {
        return try {
            appSettingsDataStore.data.first()[longPreferencesKey(key)] ?: 0L
        } catch (e: Exception) {
            Arbor.e("getStringAppSetting ERROR: $e")
            0L
        }
    }
    //endregion


    //region getStringAppSetting()
    override suspend fun getStringAppSetting(key: String): String {
        return try {
            appSettingsDataStore.data.first()[stringPreferencesKey(key)] ?: ""
        } catch (e: Exception) {
            Arbor.e("getStringAppSetting ERROR: $e")
            ""
        }
    }
    //endregion


    //region getStringAppSettingFlow()
    override suspend fun getStringAppSettingTheme(key: String): Flow<Themes> {
        return appSettingsDataStore.data.map { preferences ->
            Themes.entries.find { theme ->
                preferences[stringPreferencesKey(key)].toString() == theme.toString()
            } ?: Themes.Default
        }
    }
    //endregion


    //region setBooleanAppSetting
    override suspend fun setBooleanAppSetting(key: String, value: Boolean) {
        appSettingsDataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }
    //endregion


    //region setLongAppSetting()
    override suspend fun setLongAppSetting(key: String, value: Long) {
        appSettingsDataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }
    //endregion


    //region setStringAppSetting()
    override suspend fun setStringAppSetting(key: String, value: String) {
        appSettingsDataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }
    //endregion


    //region removeAppSetting()
    override suspend fun removeAppSetting(key: String, type: String) {
        when (type) {
            AppSettingsType.Boolean.toString() -> appSettingsDataStore.edit { it.remove(booleanPreferencesKey(key)) }
            AppSettingsType.Long.toString() -> appSettingsDataStore.edit { it.remove(longPreferencesKey(key)) }
            else -> appSettingsDataStore.edit { it.remove(stringPreferencesKey(key)) }
        }
    }
    //endregion

}