package com.easylists.presentation.models

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.MasterListsAction
import com.easylists.presentation.common.SortCrossedOffItems
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ListDetailsState @OptIn(ExperimentalUuidApi::class) constructor(
    var actionButtonState: MasterListsAction = MasterListsAction.None,
    var addEditMode: AddEditMode = AddEditMode.Add,
    var capitalization: Capitalization = Capitalization.NoCapitalization,
    var categoryList: List<EasyListsCategory> = emptyList(),
    var categoryText: String = "",
    var easyListsTagList: List<EasyListsTag> = emptyList(),
    var enableCamera: Boolean = true,
    var enablePhotos: Boolean = true,
    var enableTags: Boolean = true,
    var expandTagPills: Boolean = false,
    var groupCrossedOffItems: GroupCrossedOffItems = GroupCrossedOffItems.AllTogether,
    var groupedItemList: Map<Pair<Boolean?, String?>, List<EasyListsListItem>>? = null,
    var isPullToRefreshing: Boolean = false,
    val listItemList: List<EasyListsListItem> = emptyList(),
    var itemUid: String = Uuid.random().toString(),
    var itemName: String = "",
    var itemNameInvalid: Boolean = false,
    var itemNameInvalidMessage: String = "",
    var itemNotes: String = "",
    var itemPhotoOffset: Offset = Offset.Zero,
    val itemPhotoUri: String? = null,
    var itemPhotoScale: Double = 1.0,
    var itemQuantity: String = "",
    var itemQuantityInvalid: Boolean = false,
    var itemQuantityInvalidMessage: String = "",
    var listName: String = "",
    var listUid: String = "",
    var nextDataFetchStage: String = "category",
    var photoOffset: Offset = Offset.Zero,
    var photoScale: Double = 1.0,
    var photoUri: String? = null,
    var selectedCategoryIndex: Int = -1,
    var selectedItemUid: String = "",
    var selectedTagIds: List<Int> = emptyList(),
    var showConfirmationDialogCrossedOffItems: Boolean = false,
    var showConfirmationDialogDeleteListItem: Boolean = false,
    var showListItemBottomSheet: Boolean = false,
    var sortCrossedOffItems: SortCrossedOffItems = SortCrossedOffItems.MostRecentOnTop,
    var tagListItemList: List<TagListItem> = emptyList(),
    val tempCameraFileUrl: Uri? = null,
    var uiState: ListListUiState = ListListUiState.Idle
)


sealed interface ListItemListUiState {
    object Idle : ListItemListUiState
    data class Refreshing(val isAutomaticRefresh: Boolean) : ListItemListUiState
    data class Error(val message: String) : ListItemListUiState
}
