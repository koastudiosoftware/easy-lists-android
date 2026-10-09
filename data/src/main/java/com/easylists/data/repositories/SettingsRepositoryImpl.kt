package com.easylists.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.easylists.domain.common.AppSettings
import com.easylists.domain.common.Capitalization
import com.easylists.domain.common.GroupCrossedOffItems
import com.easylists.domain.common.SortCrossedOffItems
import com.easylists.domain.common.Themes
import com.easylists.domain.common.ViewMode
import com.easylists.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val appSettingsDataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val capitalization = stringPreferencesKey("capitalization")
        val enableCamera = booleanPreferencesKey("enable_camera")
        val enablePhotos = booleanPreferencesKey("enable_photos")
        val enableTags = booleanPreferencesKey("enable_tags")
        val groupCrossedOffItems = stringPreferencesKey("group_crossed_off_items")
        val sortCrossedOffItems = stringPreferencesKey("sort_crossed_off_items")
        val theme = stringPreferencesKey("theme")
        val viewMode = stringPreferencesKey("view_mode")
    }

    override val settings: Flow<AppSettings> = appSettingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { it.toAppSettings() }
        .distinctUntilChanged()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        appSettingsDataStore.edit { prefs ->
            val updated = transform(prefs.toAppSettings())
            prefs[Keys.capitalization] = updated.capitalization.id
            prefs[Keys.enableCamera] = updated.enableCamera
            prefs[Keys.enablePhotos] = updated.enablePhotos
            prefs[Keys.enableTags] = updated.enableTags
            prefs[Keys.groupCrossedOffItems] = updated.groupCrossedOffItems.id
            prefs[Keys.sortCrossedOffItems] = updated.sortCrossedOffItems.id
            prefs[Keys.viewMode] = updated.viewMode.id
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            capitalization = Capitalization.from(this[Keys.capitalization])
                ?: defaults.capitalization,
            enableCamera = this[Keys.enableCamera] ?: defaults.enableCamera,
            enablePhotos = this[Keys.enablePhotos] ?: defaults.enablePhotos,
            enableTags = this[Keys.enableTags] ?: defaults.enableTags,
            groupCrossedOffItems = GroupCrossedOffItems.from(this[Keys.groupCrossedOffItems])
                ?: defaults.groupCrossedOffItems,
            sortCrossedOffItems = SortCrossedOffItems.from(this[Keys.sortCrossedOffItems])
                ?: defaults.sortCrossedOffItems,
            viewMode = ViewMode.from(this[Keys.viewMode]) ?: defaults.viewMode,
        )
    }
}