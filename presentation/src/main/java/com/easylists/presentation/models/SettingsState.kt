package com.easylists.presentation.models

import com.easylists.presentation.common.ListOfListsAction

data class SettingsState(
    var actionButtonState: ListOfListsAction = ListOfListsAction.None,
    var isPullToRefreshing: Boolean = false,
)