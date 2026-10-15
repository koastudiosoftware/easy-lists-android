package com.easylists.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// Remembers which folder the user picked for backups.
// The file lives in noBackupFilesDir, so Android's cloud backup never restores a folder URI onto
// an installation that doesn't hold the permission to use it.
@Singleton
class BackupDestinationStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { File(context.noBackupFilesDir, "backup_destination.preferences_pb") }
    )

    private val folderKey = stringPreferencesKey("folder_uri")

    suspend fun get(): Uri? = dataStore.data.first()[folderKey]?.toUri()

    suspend fun set(uri: Uri) {
        dataStore.edit { it[folderKey] = uri.toString() }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(folderKey) }
    }

}