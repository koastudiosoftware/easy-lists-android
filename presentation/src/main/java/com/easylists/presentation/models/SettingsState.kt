package com.easylists.presentation.models

import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.ListOfListsAction

data class SettingsState(
    var actionButtonState: ListOfListsAction = ListOfListsAction.None,
    var groupCrossedOffItems: GroupCrossedOffItems = GroupCrossedOffItems.AllTogether,
    var isPullToRefreshing: Boolean = false,
)