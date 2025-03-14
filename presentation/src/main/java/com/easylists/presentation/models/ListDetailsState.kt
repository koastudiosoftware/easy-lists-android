package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.common.ListOfListsAction

data class ListDetailsState(
    var actionButtonState: ListOfListsAction = ListOfListsAction.None,
    var categoryList: List<EasyListsCategory> = emptyList(),
    var isPullToRefreshing: Boolean = false,
    val listItemList: List<EasyListsListItem> = emptyList(),
    var itemName: String = "",
    var itemNameInvalid: Boolean = false,
    var itemNameInvalidMessage: String = "",
    var itemNotes: String = "",
    var listNotesInvalid: Boolean = false,
    var listNotesInvalidMessage: String = "",
    var itemQuantity: String = "",
    var itemQuantityInvalid: Boolean = false,
    var itemQuantityInvalidMessage: String = "",
    var listUid: String = "",
    var selectedListUid: String = "",
    var showAddListItemBottomSheet: Boolean = false,
    var uiState: ListListUiState = ListListUiState.Idle
)


sealed interface ListItemListUiState {
    object Idle : ListItemListUiState
    data class Refreshing(val isAutomaticRefresh: Boolean) : ListItemListUiState
    data class Error(val message: String) : ListItemListUiState
}
