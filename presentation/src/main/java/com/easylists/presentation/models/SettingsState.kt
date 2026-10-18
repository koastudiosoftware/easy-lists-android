package com.easylists.presentation.models

import com.easylists.presentation.common.ListsAction

data class SettingsUiState(
    val actionButtonState: ListsAction = ListsAction.None,
    val isPullToRefreshing: Boolean = false,
    val restartActivity: Boolean? = null,
    val isSigningIn: Boolean = false,
    val signInFailed: Boolean = false,
    val showSwitchDialog: Boolean = false
)
