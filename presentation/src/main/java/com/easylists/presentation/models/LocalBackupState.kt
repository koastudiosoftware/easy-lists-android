package com.easylists.presentation.models

import com.easylists.data.backup.BackupEntry
import com.easylists.data.backup.BackupSection

data class LocalBackupState(
    val hasFolder: Boolean = false,
    val folderLabel: String? = null,
    // Everything is ticked by default
    val sections: Set<BackupSection> = BackupSection.entries.toSet(),
    val backups: List<BackupEntry> = emptyList(),
    val isBusy: Boolean = false,
    val message: BackupMessage? = null,
    val pendingRestore: PendingRestore? = null,
)