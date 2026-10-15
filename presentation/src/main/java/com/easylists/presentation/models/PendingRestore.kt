package com.easylists.presentation.models

import android.net.Uri
import com.easylists.data.backup.BackupSummary

// A backup the user picked, waiting for them to confirm the restore
data class PendingRestore(
    val uri: Uri,
    val summary: BackupSummary,
)