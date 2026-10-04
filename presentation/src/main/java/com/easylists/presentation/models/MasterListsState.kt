package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.MasterListsAction

data class MasterListsState(
    var actionButtonState: MasterListsAction = MasterListsAction.None,
    var addEditMode: AddEditMode = AddEditMode.Add,
    var capitalization: Capitalization = Capitalization.NoCapitalization,
    var isPullToRefreshing: Boolean = false,
    var listList: List<EasyListsList>? = null,
    var listName: String = "",
    var listNameInvalid: Boolean = false,
    var listNameInvalidMessage: String = "",
    var listNotes: String = "",
    var listNotesInvalid: Boolean = false,
    var listNotesInvalidMessage: String = "",
    var listUid: String = "",
    var selectedListUid: String = "",
    var showConfirmationDialog: Boolean = false,
    var showListBottomSheet: Boolean = false,
    var uiState: ListListUiState = ListListUiState.Idle
)


sealed interface ListListUiState {
    object Idle : ListListUiState
    data class Refreshing(val isAutomaticRefresh: Boolean) : ListListUiState
    data class Error(val message: String) : ListListUiState
}
