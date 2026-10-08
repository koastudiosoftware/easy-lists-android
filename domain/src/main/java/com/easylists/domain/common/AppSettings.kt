package com.easylists.domain.common

data class AppSettings(
    val capitalization: Capitalization = Capitalization.NoCapitalization,
    val enableCamera: Boolean = true,
    val enablePhotos: Boolean = true,
    val enableTags: Boolean = true,
    val groupCrossedOffItems: GroupCrossedOffItems = GroupCrossedOffItems.AllTogether,
    val sortCrossedOffItems: SortCrossedOffItems = SortCrossedOffItems.MostRecentOnTop,
    val viewMode: ViewMode = ViewMode.Card,
)
