package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.MasterListsAction
import com.easylists.presentation.common.SortCrossedOffItems

data class ListDetailsState(
    var actionButtonState: MasterListsAction = MasterListsAction.None,
    var addEditMode: AddEditMode = AddEditMode.Add,
    var categoryList: List<EasyListsCategory> = emptyList(),
    var categoryText: String = "",
    var groupCrossedOffItems: GroupCrossedOffItems = GroupCrossedOffItems.AllTogether,
    var groupedItemList: Map<Pair<Boolean?, String?>, List<EasyListsListItem>>? = null,
    var isPullToRefreshing: Boolean = false,
    val listItemList: List<EasyListsListItem> = emptyList(),
    var itemUid: String = "",
    var itemName: String = "",
    var itemNameInvalid: Boolean = false,
    var itemNameInvalidMessage: String = "",
    var itemNotes: String = "",
    var listNotesInvalid: Boolean = false,
    var listNotesInvalidMessage: String = "",
    var itemQuantity: String = "",
    var itemQuantityInvalid: Boolean = false,
    var itemQuantityInvalidMessage: String = "",
    var listName: String = "",
    var listUid: String = "",
    var nextDataFetchStage: String = "category",
    var selectedCategoryIndex: Int = -1,
    var selectedItemUid: String = "",
    var showConfirmationDialog: Boolean = false,
    var showListItemBottomSheet: Boolean = false,
    var sortCrossedOffItems: SortCrossedOffItems = SortCrossedOffItems.MostRecentOnTop,
    var uiState: ListListUiState = ListListUiState.Idle
)


sealed interface ListItemListUiState {
    object Idle : ListItemListUiState
    data class Refreshing(val isAutomaticRefresh: Boolean) : ListItemListUiState
    data class Error(val message: String) : ListItemListUiState
}
