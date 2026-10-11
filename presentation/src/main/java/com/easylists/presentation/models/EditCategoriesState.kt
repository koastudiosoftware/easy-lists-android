package com.easylists.presentation.models

import androidx.annotation.StringRes
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.common.AddEditMode

data class EditCategoriesState(
    // ---- data from the database ----
    val categoryList: List<EasyListsCategory> = emptyList(),
    val listList: List<EasyListsList> = emptyList(),
    val listItemList: List<EasyListsListItem> = emptyList(),
    // categoryId -> number of list items using the category (precomputed once per emission)
    val categoryUsageCounts: Map<String, Int> = emptyMap(),

    // ---- multi-select ----
    val selectionMode: Boolean = false,
    val selectedCategoryIds: Set<String> = emptySet(),

    // ---- overlays (null = hidden) ----
    val categorySheet: CategorySheetState? = null,
    val pendingDelete: CategoryPendingDelete? = null,

    // one-shot message shown in the snackbar, cleared by onMessageShown()
    @StringRes val messageRes: Int? = null,
) {

    /** True when the name typed in the sheet collides (case-insensitive) with ANOTHER tag. */
    val isTagNameDuplicate: Boolean
        get() {
            val sheet = categorySheet ?: return false
            val name = sheet.name.trim()
            return name.isNotEmpty() && categoryList.any {
                it.categoryId != sheet.categoryId && it.name.equals(name, ignoreCase = true)
            }
        }

    val canSaveCategory: Boolean
        get() {
            val sheet = categorySheet ?: return false
            if (sheet.isSaving) return false
            val name = sheet.name.trim()
            if (name.isEmpty() || isTagNameDuplicate) return false
            if (sheet.mode == AddEditMode.Edit) {
                val current = categoryList.firstOrNull { it.categoryId == sheet.categoryId } ?: return false
                // exact compare on purpose: "dairy" -> "Dairy" is a valid edit
                return name != current.name
            }
            return true
        }
}

/** Add/edit tag bottom sheet. */
data class CategorySheetState(
    val mode: AddEditMode,
    val categoryId: String? = null,
    val name: String = "",
    val isSaving: Boolean = false,
)

sealed interface CategoryPendingDelete {
    data class Single(val categoryId: String) : CategoryPendingDelete
    data object Selected : CategoryPendingDelete
}

/** One list that uses a tag, with the items of that list carrying the tag. */
data class CategoryListUsage(
    val list: EasyListsList,
    val items: List<EasyListsListItem>,
)

/** Lists (and their items) that use the given tag. Computed once per call, not per list. */
fun EditCategoriesState.usageFor(categoryId: String?): List<CategoryListUsage> {
    if (categoryId == null) return emptyList()

    val itemsByList = listItemList
        .filter { it.categoryId == categoryId }
        .groupBy { it.listId }

    return listList
        .filter { it.listId in itemsByList }
        .map { list -> CategoryListUsage(list = list, items = itemsByList[list.listId].orEmpty()) }
}
