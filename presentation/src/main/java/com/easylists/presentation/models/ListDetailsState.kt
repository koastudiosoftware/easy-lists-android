package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.common.ListOfListsAction

data class ListDetailsState(
    var actionButtonState: ListOfListsAction = ListOfListsAction.None,
    var isPullToRefreshing: Boolean = false,
    val listItemList: List<EasyListsListItem> = emptyList(),
    var listName: String = "",
    var listNameInvalid: Boolean = false,
    var listNameInvalidMessage: String = "",
    var listNotes: String = "",
    var listNotesInvalid: Boolean = false,
    var listNotesInvalidMessage: String = "",
    var listUid: String = "",
    var selectedListUid: String = "",
    var showAddListBottomSheet: Boolean = false,
    var uiState: ListListUiState = ListListUiState.Idle
)


sealed interface ListItemListUiState {
    object Idle : ListItemListUiState
    data class Refreshing(val isAutomaticRefresh: Boolean) : ListItemListUiState
    data class Error(val message: String) : ListItemListUiState
}
