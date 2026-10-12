package com.easylists.presentation.models

import com.easylists.domain.common.AppSettings
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.presentation.common.AddEditMode

sealed interface ListItemsUiState {
    object Loading : ListItemsUiState

    data class Success(
        val rows: List<ListRow>,
        val items: List<EasyListsListItem>,        // not deleted; used for validation
        val categories: List<EasyListsCategory>,
        val tags: List<EasyListsTag>,
        val tagLinks: List<TagListItem>,
        val settings: AppSettings,
    ) : ListItemsUiState

    data class Error(val message: String) : ListItemsUiState
}


//region ListRow
sealed interface ListRow {
    val key: String

    // categoryName == null means "uncategorized" (the UI supplies the localized title)
    data class CategoryHeader(val categoryName: String?, val crossedOff: Boolean) : ListRow {
        override val key = "header_${crossedOff}_${categoryName ?: "uncategorized"}"
    }

    object CrossedOffHeader : ListRow {
        override val key = "crossed_off_header"
    }

    data class Item(val item: EasyListsListItem, val tags: List<EasyListsTag>) : ListRow {
        override val key = "item_${item.listItemId}"
    }

    object DeleteCrossedOff : ListRow {
        override val key = "delete_crossed_off"
    }
}
//endregion


//region Photo state
data class PhotoTransform(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
)


// FramedPhoto is given `initial` and reports into `current`; see the note above.
data class PhotoDraft(
    val uri: String,
    val initial: PhotoTransform = PhotoTransform(),
    val current: PhotoTransform = initial,
)
//endregion


//region ItemEditorState
data class ItemEditorState(
    val itemId: String,
    val original: EasyListsListItem? = null,    // non-null when editing
    val name: String = "",
    val quantity: String = "",
    val notes: String = "",
    val categoryName: String = "",
    val selectedTagIds: Set<String> = emptySet(),
    val photo: PhotoDraft? = null,
) {
    val mode: AddEditMode get() = if (original == null) AddEditMode.Add else AddEditMode.Edit
}

enum class ItemNameError { Duplicate }

data class ItemEditorValidation(val nameError: ItemNameError?, val canSave: Boolean)

fun ItemEditorState.validate(existing: List<EasyListsListItem>): ItemEditorValidation {
    val trimmed = name.trim()
    val duplicate = trimmed.isNotEmpty() && existing.any {
        it.listItemId != itemId && it.name.equals(trimmed, ignoreCase = true)
    }
    return ItemEditorValidation(
        nameError = if (duplicate) ItemNameError.Duplicate else null,
        canSave = trimmed.isNotEmpty() && !duplicate,
    )
}
//endregion


data class ListItemsInteractionState(
    val selectedItemId: String? = null,
    val showDeleteItemDialog: Boolean = false,
    val showDeleteCrossedOffDialog: Boolean = false,
    val expandTagPills: Boolean = false,
    val isRefreshing: Boolean = false,
)