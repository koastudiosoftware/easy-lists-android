package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.MasterListsAction

sealed interface ListListUiState {
    object Loading : ListListUiState
    data class Success(val lists: List<EasyListsList>) : ListListUiState
    data class Error(val message: String) : ListListUiState
}

data class ListEditorState(
    val mode: AddEditMode = AddEditMode.Add,
    val listId: String = "",
    val name: String = "",
    val notes: String = "",
)

data class ListsInteractionState(
    val selectedListId: String? = null,
    val actionButtonState: MasterListsAction = MasterListsAction.None,
    val showConfirmationDialog: Boolean = false,
    val isRefreshing: Boolean = false,
)

enum class ListNameError { Duplicate }

data class ListEditorValidation(
    val nameError: ListNameError?,
    val canSave: Boolean,
)

fun ListEditorState.validate(existing: List<EasyListsList>): ListEditorValidation {
    val trimmed = name.trim()
    val duplicate = trimmed.isNotEmpty() && existing.any {
        it.listId != listId && it.name.equals(trimmed, ignoreCase = true)
    }
    return ListEditorValidation(
        nameError = if (duplicate) ListNameError.Duplicate else null,
        canSave = trimmed.isNotEmpty() && !duplicate,
    )
}