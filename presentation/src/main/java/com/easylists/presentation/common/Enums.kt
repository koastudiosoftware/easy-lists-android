package com.easylists.presentation.common

import com.easylists.domain.common.AppSettingsType

enum class AddEditMode() {
    Add,
    Edit,
}

//region AppSettingsKeys
enum class AppSettingsKeys(val key: String, val type: AppSettingsType) {
    GroupCrossedOffItems("GroupCrossedOffItems", AppSettingsType.String),
    SortCrossedOffItems("SortCrossedOffItems", AppSettingsType.String),
    Theme("Theme", AppSettingsType.String),
}
//endregion


//region EditCategoriesAction
enum class EditCategoriesAction(val value: String) {
    Remove("Remove"),
    None("None"),
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


//region MasterListsAction
enum class MasterListsAction(val value: String) {
    Add("Add"),
    None("None"),
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
