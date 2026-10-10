package com.easylists.presentation.models

import com.easylists.presentation.common.ListsAction

data class SettingsUiState(
    val actionButtonState: ListsAction = ListsAction.None,
    val isPullToRefreshing: Boolean = false,
    val restartActivity: Boolean? = null,
)
