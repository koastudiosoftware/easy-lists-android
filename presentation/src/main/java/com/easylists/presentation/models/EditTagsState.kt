package com.easylists.presentation.models

import androidx.annotation.StringRes
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.presentation.common.AddEditMode

data class EditTagsState(
    // ---- data from the database ----
    val tagList: List<EasyListsTag> = emptyList(),
    val listList: List<EasyListsList> = emptyList(),
    val listItemList: List<EasyListsListItem> = emptyList(),
    val tagListItemList: List<TagListItem> = emptyList(),
    // tagId -> number of list items using the tag (precomputed once per emission)
    val tagUsageCounts: Map<String, Int> = emptyMap(),

    // ---- multi-select ----
    val selectionMode: Boolean = false,
    val selectedTagIds: Set<String> = emptySet(),

    // ---- overlays (null = hidden) ----
    val tagSheet: TagSheetState? = null,
    val colorEditor: ColorEditorState? = null,
    val pendingDelete: TagPendingDelete? = null,

    // one-shot message shown in the snackbar, cleared by onMessageShown()
    @StringRes val messageRes: Int? = null,
) {

    /** True when the name typed in the sheet collides (case-insensitive) with ANOTHER tag. */
    val isTagNameDuplicate: Boolean
        get() {
            val sheet = tagSheet ?: return false
            val name = sheet.name.trim()
            return name.isNotEmpty() && tagList.any {
                it.tagId != sheet.tagId && it.name.equals(name, ignoreCase = true)
            }
        }

    val canSaveTag: Boolean
        get() {
            val sheet = tagSheet ?: return false
            if (sheet.isSaving) return false
            val name = sheet.name.trim()
            if (name.isEmpty() || isTagNameDuplicate) return false
            if (sheet.mode == AddEditMode.Edit) {
                val current = tagList.firstOrNull { it.tagId == sheet.tagId } ?: return false
                // exact compare on purpose: "milk" -> "Milk" is a valid edit
                return name != current.name
            }
            return true
        }
}

/** Add/edit tag bottom sheet. */
data class TagSheetState(
    val mode: AddEditMode,
    val tagId: String? = null,
    val name: String = "",
    val isSaving: Boolean = false,
)

/**
 * Color picker bottom sheet.
 * hexCode  = "#AARRGGBB" (uppercase), the value that gets stored
 * hexInput = the 6 digits shown in the text field (may be partial while typing)
 * hexSyncCount increments only when a complete hex value was typed, so the screen knows
 * when to push the typed color into the wheel.
 */
data class ColorEditorState(
    val tagId: String,
    val hexCode: String,
    val hexInput: String,
    val hexSyncCount: Int = 0,
)

sealed interface TagPendingDelete {
    data class Single(val tagId: String) : TagPendingDelete
    data object Selected : TagPendingDelete
}

/** One list that uses a tag, with the items of that list carrying the tag. */
data class ListUsage(
    val list: EasyListsList,
    val items: List<EasyListsListItem>,
)

/** Lists (and their items) that use the given tag. Computed once per call, not per list. */
fun EditTagsState.usageFor(tagId: String?): List<ListUsage> {
    if (tagId == null) return emptyList()

    val itemIds = tagListItemList
        .filter { it.tagId == tagId }
        .map { it.listItemId }
        .toSet()
    if (itemIds.isEmpty()) return emptyList()

    val itemsByList = listItemList
        .filter { it.listItemId in itemIds }
        .groupBy { it.listId }

    return listList
        .filter { it.listId in itemsByList }
        .map { list -> ListUsage(list = list, items = itemsByList[list.listId].orEmpty()) }
}
