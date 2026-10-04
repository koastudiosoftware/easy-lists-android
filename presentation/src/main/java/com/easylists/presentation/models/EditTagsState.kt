package com.easylists.presentation.models

import androidx.compose.ui.graphics.Color
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.EditTagsAction
import com.easylists.presentation.common.toHexCodeWithAlpha

data class EditTagsState(
    var actionButtonState: EditTagsAction = EditTagsAction.None,
    var addEditMode: AddEditMode = AddEditMode.Add,
    var capitalization: Capitalization = Capitalization.NoCapitalization,
    var confirmationOnDismissRequest: () -> Unit = {},
    var confirmationOnConfirmation: () -> Unit = {},
    var confirmationTitle: String = "",
    var confirmationMessage: String = "",
    var deselectCheckboxes: Boolean = false,
    var isPullToRefreshing: Boolean = false,
    var listItemList: List<EasyListsListItem> = emptyList(),
    var listList: List<EasyListsList> = emptyList(),
    var nextStep: String? = null,
    var selectedHexCode: String = Color.White.toHexCodeWithAlpha(),
    var selectedItem: EasyListsTag? = null,
    var showColorPickerBottomSheet: Boolean = false,
    var showTagBottomSheet: Boolean = false,
    var showConfirmationDialog: Boolean = false,
    var showContextItems: Boolean = false,
    var tagList: List<EasyListsTag> = emptyList(),
    var tagListItemList: List<TagListItem> = emptyList(),
    var tagName: String = "",
    var tagNameInvalid: Boolean = false,
    var tagNameInvalidMessage: String = "",
    var textFieldHexCode: String = "",
    var userUpdatedHexCode: Boolean = false,
)
