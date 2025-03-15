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
    GroupCrossedOffItems("GroupCrossedOffItems", AppSettingsType.String)
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
