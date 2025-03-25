package com.easylists.presentation.models

import com.easylists.domain.models.Themes
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.MasterListsAction
import com.easylists.presentation.common.SortCrossedOffItems

data class SettingsState(
    var actionButtonState: MasterListsAction = MasterListsAction.None,
    var capitalization: Capitalization = Capitalization.NoCapitalization,
    var enableCamera: Boolean = true,
    var groupCrossedOffItems: GroupCrossedOffItems = GroupCrossedOffItems.AllTogether,
    var isPullToRefreshing: Boolean = false,
    var restartActivity: Boolean? = null,
    var sortCrossedOffItems: SortCrossedOffItems = SortCrossedOffItems.MostRecentOnTop,
    var theme: Themes = Themes.Solarized,
)
