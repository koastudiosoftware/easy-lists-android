package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.EditCategoriesAction

data class EditCategoriesState(
    var actionButtonState: EditCategoriesAction = EditCategoriesAction.None,
    var addEditMode: AddEditMode = AddEditMode.Add,
    var capitalization: Capitalization = Capitalization.NoCapitalization,
    var categoryList: List<EasyListsCategory> = emptyList(),
    var categoryName: String = "",
    var categoryNameInvalid: Boolean = false,
    var categoryNameInvalidMessage: String = "",
    var deselectCheckboxes: Boolean = false,
    var isPullToRefreshing: Boolean = false,
    var listItemList: List<EasyListsListItem> = emptyList(),
    var listList: List<EasyListsList> = emptyList(),
    var nextStep: String? = null,
    var selectedItem: EasyListsCategory? = null,
    var showCategoryBottomSheet: Boolean = false,
    var showConfirmationDialog: Boolean = false,
    var showContextItems: Boolean = false,
)