package com.easylists.presentation.models

import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.MasterListsAction
import com.easylists.presentation.common.SortCrossedOffItems

data class SettingsState(
    var actionButtonState: MasterListsAction = MasterListsAction.None,
    var groupCrossedOffItems: GroupCrossedOffItems = GroupCrossedOffItems.AllTogether,
    var isPullToRefreshing: Boolean = false,
    var sortCrossedOffItems: SortCrossedOffItems = SortCrossedOffItems.MostRecentOnTop,
)