package com.easylists.presentation.ui.backup

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.data.backup.BackupDestinationException
import com.easylists.data.backup.BackupDestinationStore
import com.easylists.data.backup.BackupEntry
import com.easylists.data.backup.BackupFileManager
import com.easylists.data.backup.BackupFormatException
import com.easylists.data.backup.BackupSection
import com.easylists.presentation.R
import com.easylists.presentation.models.BackupMessage
import com.easylists.presentation.models.LocalBackupState
import com.easylists.presentation.models.PendingRestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalBackupViewModel @Inject constructor(
    private val backupFileManager: BackupFileManager,
    private val destinationStore: BackupDestinationStore,
) : ViewModel() {

    private val _state = MutableStateFlow(LocalBackupState())
    val state: StateFlow<LocalBackupState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            destinationStore.getSections()?.let { saved ->
                val sections = saved.mapNotNull { BackupSection.fromId(it) }.toSet()
                _state.update { it.copy(sections = sections) }
            }
            load()
        }
    }

    fun toggleSection(section: BackupSection) {
        val current = _state.value.sections
        val sections = if (section in current) current - section else current + section
        _state.update { it.copy(sections = sections) }
        viewModelScope.launch {
            destinationStore.setSections(sections.map { it.id }.toSet())
        }
    }

    // Call with the URI from the folder picker
    fun onFolderPicked(uri: Uri) {
        runOperation {
            backupFileManager.setDestination(uri)
            null
        }
    }

    fun createBackup() {
        val current = _state.value
        if (current.isBusy || !current.hasFolder || current.sections.isEmpty()) return
        runOperation {
            val created = backupFileManager.createBackup(current.sections)
            if (created.missingPhotos > 0) {
                BackupMessage(
                    isError = true,
                    textRes = R.string.backup_done_missing_photos,
                    args = listOf(created.name, created.missingPhotos),
                )
            } else {
                BackupMessage(isError = false, textRes = R.string.backup_done, args = listOf(created.name))
            }
        }
    }

    // Call with a backup from the list or from the file picker. Nothing changes until confirmRestore().
    fun onBackupChosen(uri: Uri) {
        if (_state.value.isBusy) return
        runOperation {
            val summary = backupFileManager.readBackup(uri).summary()
            _state.update { it.copy(pendingRestore = PendingRestore(uri, summary)) }
            null
        }
    }

    fun confirmRestore() {
        val pending = _state.value.pendingRestore ?: return
        _state.update { it.copy(pendingRestore = null) }
        runOperation {
            backupFileManager.restore(pending.uri)
            BackupMessage(isError = false, textRes = R.string.backup_restored)
        }
    }

    fun cancelRestore() {
        _state.update { it.copy(pendingRestore = null) }
    }

    // Runs one operation with the busy indicator on, turns failures into a message, then reloads the folder.
    // The block returns the message to show, or null for none.
    private fun runOperation(block: suspend () -> BackupMessage?) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, message = null) }
            val message = try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: BackupDestinationException) {
                BackupMessage(isError = true, textRes = R.string.backup_error_folder_lost)
            } catch (e: BackupFormatException) {
                errorMessage(R.string.backup_error_restore_failed, e)
            } catch (e: Exception) {
                errorMessage(R.string.backup_error_generic, e)
            }
            _state.update { it.copy(isBusy = false, message = message) }
            load()
        }
    }

    private suspend fun load() {
        val folder = backupFileManager.destination()
        var folderUsable = folder != null
        var backups = emptyList<BackupEntry>()
        var message: BackupMessage? = null

        if (folder != null) {
            try {
                backups = backupFileManager.listBackups()
            } catch (e: BackupDestinationException) {
                folderUsable = false
                message = BackupMessage(isError = true, textRes = R.string.backup_error_folder_lost)
            }
        }

        _state.update {
            it.copy(
                hasFolder = folderUsable,
                folderLabel = if (folderUsable && folder != null) describe(folder) else null,
                backups = backups,
                message = message ?: it.message,
            )
        }
    }

    // "primary:Documents/Backups" -> "Documents/Backups"
    private fun describe(tree: Uri): String {
        val id = tree.lastPathSegment ?: return tree.toString()
        return id.substringAfter(':', id).ifBlank { id }
    }

    private fun errorMessage(@StringRes withDetail: Int, e: Exception): BackupMessage {
        val detail = e.message
        return if (detail.isNullOrBlank()) {
            BackupMessage(isError = true, textRes = R.string.backup_error_unknown)
        } else {
            BackupMessage(isError = true, textRes = withDetail, args = listOf(detail))
        }
    }

}