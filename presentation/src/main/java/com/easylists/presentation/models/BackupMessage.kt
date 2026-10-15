package com.easylists.presentation.models

import androidx.annotation.StringRes

// A one-line result shown at the top of the sheet after an operation finishes
data class BackupMessage(
    val isError: Boolean,
    @StringRes val textRes: Int,
    val args: List<Any> = emptyList(),
)