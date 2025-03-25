package com.easylists.presentation.common

import androidx.compose.ui.text.input.KeyboardCapitalization
import com.easylists.domain.common.AppSettingsType

//region AddEditMode
enum class AddEditMode() {
    Add,
    Edit,
}
//endregion


//region Capitalization
enum class Capitalization(val value: String, val keyboardCapitalization: KeyboardCapitalization) {
    NoCapitalization("No capitalization", KeyboardCapitalization.None),
    CapitalizeFirstLetter("Capitalize first letter", KeyboardCapitalization.Sentences),
    CapitalizeAllLetters("Capitalize all letters", KeyboardCapitalization.Words),
    ;

    override fun toString(): String {
        return value
    }

    companion object {
        infix fun from(value: String): Capitalization? =
            Capitalization.entries.firstOrNull { it.value == value }
    }
}
//endregion


//region AppSettingsKeys
enum class AppSettingsKeys(val key: String, val type: AppSettingsType) {
    Capitalization("Capitalization", AppSettingsType.String),
    EnableCamera("EnableCamera", AppSettingsType.Boolean),
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


//region EditTagsAction
enum class EditTagsAction(val value: String) {
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
    MostRecentOnTop("Most recently crossed off on top"),
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
