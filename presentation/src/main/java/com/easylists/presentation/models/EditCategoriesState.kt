package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.common.EditCategoriesAction
import com.easylists.presentation.common.MasterListsAction

data class EditCategoriesState(
    var actionButtonState: EditCategoriesAction = EditCategoriesAction.None,
    var categoryList: List<EasyListsCategory> = emptyList(),
    var isPullToRefreshing: Boolean = false,
    var listItemList: List<EasyListsListItem> = emptyList(),
    var selectedItem: EasyListsCategory? = null,
    var showCategoryBottomSheet: Boolean = false,
    var showConfirmationDialog: Boolean = false,
    var showContextItems: Boolean = false,
)