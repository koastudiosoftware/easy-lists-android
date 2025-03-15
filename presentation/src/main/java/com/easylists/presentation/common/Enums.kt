package com.easylists.presentation.common

import com.easylists.domain.common.AppSettingsType

//region ListOfListsAction
enum class ListOfListsAction(val value: String) {
    Add("Add"),
    None("None"),
}
//endregion


//region AppSettingsKeys
enum class AppSettingsKeys(val key: String, val type: AppSettingsType) {
    GroupCrossedOffItems("GroupCrossedOffItems", AppSettingsType.String),
    SortCrossedOffItems("SortCrossedOffItems", AppSettingsType.String),
}
//endregion


//region GroupCrossedOffItems
enum class GroupCrossedOffItems(val value: String) {
    AllTogether("All together"),
    ByCategory("By category"),
    ;

    override fun toString(): String {
        return value
    }

    companion object {
        infix fun from(value: String): GroupCrossedOffItems? =
            GroupCrossedOffItems.entries.firstOrNull { it.value == value }
    }
}
//endregion


//region SortCrossedOffItems
enum class SortCrossedOffItems(val value: String) {
    Alphabetically("Alphabetically"),
    MostRecentOnTop("Most recent on top"),
    ;

    override fun toString(): String {
        return value
    }

    companion object {
        infix fun from(value: String): SortCrossedOffItems? =
            SortCrossedOffItems.entries.firstOrNull { it.value == value }
    }
}
//endregion
