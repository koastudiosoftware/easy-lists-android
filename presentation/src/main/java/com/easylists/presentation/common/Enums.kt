package com.easylists.presentation.common

//region AddEditMode
enum class AddEditMode() {
    Add,
    Edit,
}
//endregion


//region EditCategoriesAction
enum class EditCategoriesAction(val value: String) {
    Delete("Delete"),
    None("None"),
}
//endregion


//region EditTagsAction
enum class EditTagsAction(val value: String) {
    Delete("Delete"),
    None("None"),
}
//endregion


//region MasterListsAction
enum class MasterListsAction(val value: String) {
    Add("Add"),
    None("None"),
}
//endregion
