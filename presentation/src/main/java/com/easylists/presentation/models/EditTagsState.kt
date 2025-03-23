package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.EditTagsAction

data class EditTagsState(
    var actionButtonState: EditTagsAction = EditTagsAction.None,
    var addEditMode: AddEditMode = AddEditMode.Add,
    var capitalization: Capitalization = Capitalization.NoCapitalization,
    var deselectCheckboxes: Boolean = false,
    var isPullToRefreshing: Boolean = false,
    var listItemList: List<EasyListsListItem> = emptyList(),
    var nextStep: String? = null,
    var selectedItem: EasyListsTag? = null,
    var showTagBottomSheet: Boolean = false,
    var showConfirmationDialog: Boolean = false,
    var showContextItems: Boolean = false,
    var easyListsTagList: List<EasyListsTag> = emptyList(),
    var tagName: String = "",
    var tagNameInvalid: Boolean = false,
    var tagNameInvalidMessage: String = "",
)
