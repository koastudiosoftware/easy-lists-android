package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.common.ListOfListsAction

data class ListOfListsState(
    var actionButtonState: ListOfListsAction = ListOfListsAction.None,
    var isPullToRefreshing: Boolean = false,
    var listList: List<EasyListsList>? = null,
    var listName: String = "",
    var listNameInvalid: Boolean = false,
    var listNameInvalidMessage: String = "",
    var listNotes: String = "",
    var listNotesInvalid: Boolean = false,
    var listNotesInvalidMessage: String = "",
    var selectedListUid: String = "",
    var showAddListBottomSheet: Boolean = false,
    var uiState: ListListUiState = ListListUiState.Idle
)


sealed interface ListListUiState {
    object Idle : ListListUiState
    data class Refreshing(val isAutomaticRefresh: Boolean) : ListListUiState
    data class Error(val message: String) : ListListUiState
}
