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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val appSettingsDataStore: DataStore<Preferences>
) : SettingsRepository {

    // One-shot reads
    override suspend fun getBooleanAppSetting(key: String): String =
        appSettingsDataStore.data.first()[booleanPreferencesKey(key)]?.toString().orEmpty()

    override suspend fun getLongAppSetting(key: String): Long =
        appSettingsDataStore.data.first()[longPreferencesKey(key)] ?: 0L

    override suspend fun getStringAppSetting(key: String): String =
        appSettingsDataStore.data.first()[stringPreferencesKey(key)].orEmpty()

    //region getStringAppSettingFlow()
    override suspend fun getStringAppSettingTheme(key: String): Flow<Themes> {
        return appSettingsDataStore.data.map { preferences ->
            Themes.entries.find { theme ->
                preferences[stringPreferencesKey(key)].toString() == theme.toString()
            } ?: Themes.Default
        }
    }
    //endregion

    // Observed reads
    override fun getAppSettingFlow(key: String, type: String): Flow<String> =
        appSettingsDataStore.data
            .map { prefs ->
                when (type) {
                    AppSettingsType.Boolean.toString() ->
                        prefs[booleanPreferencesKey(key)]?.toString()
                    AppSettingsType.Long.toString() ->
                        prefs[longPreferencesKey(key)]?.toString()
                    else -> prefs[stringPreferencesKey(key)]
                }.orEmpty()
            }
            .distinctUntilChanged()

    // Writes
    override suspend fun setBooleanAppSetting(key: String, value: Boolean) {
        appSettingsDataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    override suspend fun setLongAppSetting(key: String, value: Long) {
        appSettingsDataStore.edit { it[longPreferencesKey(key)] = value }
    }

    override suspend fun setStringAppSetting(key: String, value: String) {
        appSettingsDataStore.edit { it[stringPreferencesKey(key)] = value }
    }


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
