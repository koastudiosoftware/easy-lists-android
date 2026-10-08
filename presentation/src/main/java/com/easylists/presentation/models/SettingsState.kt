package com.easylists.presentation.models

import com.easylists.presentation.common.MasterListsAction

data class SettingsUiState(
    val actionButtonState: MasterListsAction = MasterListsAction.None,
    val isPullToRefreshing: Boolean = false,
    val restartActivity: Boolean? = null,
)
