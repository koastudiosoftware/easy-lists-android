package com.easylists.presentation.ui.edittags

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.EasyListsTag
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.EditTagsAction
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.AppTextField
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.common.toHexCodeWithAlpha
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsCancel
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.MaterialIconsDelete
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagsScreen(
    navController: NavController<Screen>,
    viewModel: EditTagsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val s = settings ?: return

    val snackbarHostState = remember { SnackbarHostState() }

    when {
        viewModel.state.nextStep == "remove_tags" -> {
            viewModel.removeTags()
        }
    }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { EditTagsScreenTitle() },
                navigationIcon = { EditTagsScreenTopAppBarNavigationIcon(navController) },
                actions = { EditTagsScreenActionIcons(viewModel) },
            )
        }
    ) { innerPadding ->

        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = viewModel.state.isPullToRefreshing,
            onRefresh = viewModel.onPullToRefresh(),
            state = pullToRefreshState,
            modifier = Modifier.padding(innerPadding),
        ) {

            ConfirmRemove(viewModel)

            EditTagsScreenCategoryBottomSheet(viewModel)

            EditTagsScreenColorPickerBottomSheet(viewModel)

            EditTagsScreenContent(viewModel)

        }
    }

}


//region EditTagsScreenTitle
@Composable
fun EditTagsScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.edit_tags))
    }
}
//endregion


//region EditTagsScreenTopAppBarNavigationIcon
@Composable
fun EditTagsScreenTopAppBarNavigationIcon(navController: NavController<Screen>) {
    IconButton(
        onClick = { navController.pop() }
    ) {
        Icon(
            imageVector = MaterialIconsArrowBack,
            contentDescription = stringResource(R.string.return_to_previous_screen),
        )
    }
}
//endregion


//region EditTagsScreenActionIcons
@Composable
fun EditTagsScreenActionIcons(viewModel: EditTagsViewModel) {
    when (viewModel.state.actionButtonState) {
        EditTagsAction.Delete -> {
            val title = stringResource(R.string.confirm_deletion)
            val message = stringResource(R.string.delete_tags_warning)
            IconButton(
                enabled = viewModel.state.tagList.any { it.selectedForRemoval },
                onClick = {
                    viewModel.configureDeleteTag(
                        title = title,
                        message = message,
                        onConfirmation = {
                            viewModel.deselectCheckboxes()
                            viewModel.removeTagFromListItems()
                            viewModel.setShowConfirmationDialogState(false)
                        },
                        onDismissRequest = {
                            viewModel.dismissConfirmationDialog()
                        }
                    )
                }
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = MaterialIconsDelete,
                    contentDescription = stringResource(R.string.delete_selected_tags)
                )
            }
            IconButton(onClick = {
                viewModel.showContextIcons()
            }) {
                Icon(
                    modifier = Modifier,
                    imageVector = MaterialIconsCancel,
                    contentDescription = stringResource(R.string.cancel_deletion_of_selected_tags)
                )
            }
        }

        else -> {
            IconButton(onClick = {
                viewModel.showTagBottomSheet()
            }) {
                Icon(
                    modifier = Modifier,
                    imageVector = MaterialIconsAdd,
                    contentDescription = stringResource(R.string.create_new_tag)
                )
            }
        }
    }
}
//endregion


//region EditTagsScreenCategoryBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagsScreenCategoryBottomSheet(viewModel: EditTagsViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember { mutableStateOf(false) }

    when (viewModel.state.showTagBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet.value = false
                viewModel.onTagBottomSheetDismiss()
            },
            dragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BottomSheetDefaults.DragHandle()
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                LazyColumn(modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large)) {
                    item {
                        SectionTitle(
                            title = stringResource(
                                if (viewModel.state.addEditMode == AddEditMode.Add) R.string.add_tag
                                else R.string.edit_tag
                            ),
                            icon = {
                                IconButton(
                                    enabled = viewModel.tagIconButtonEnabled(),
                                    onClick = {
                                        if (viewModel.state.addEditMode == AddEditMode.Add) viewModel.addTag()
                                        else viewModel.updateTag()
                                    },
                                ) {
                                    Icon(
                                        imageVector = MaterialIconsCheck,
                                        contentDescription = stringResource(R.string.add_tag),
                                    )
                                }
                                if (viewModel.state.addEditMode == AddEditMode.Edit) {
                                    val title = stringResource(R.string.confirm_deletion)
                                    val message = stringResource(R.string.delete_tag_warning)
                                    IconButton(
                                        enabled = true,
                                        onClick = {
                                            viewModel.configureDeleteTag(
                                                title = title,
                                                message = message,
                                                onConfirmation = {
                                                    viewModel.onTagSelectedForRemovalChanged(
                                                        viewModel.state.selectedItem?.tagId
                                                    )
                                                    viewModel.removeTags()
                                                    viewModel.dismissConfirmationDialog()
                                                    viewModel.showTagBottomSheet()
                                                },
                                                onDismissRequest = {
                                                    viewModel.dismissConfirmationDialog()
                                                }
                                            )
                                        },
                                    ) {
                                        Icon(
                                            imageVector = MaterialIconsDelete,
                                            contentDescription = stringResource(R.string.add_tag),
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                        )
                    }

                    item {
                        EditTagsScreenBottomSheetName(viewModel)
                    }

                    item {
                        EditTagsScreenBottomSheetListsAndItems(viewModel)
                    }

                }
            }
        }
    }
}
//endregion


//region EditTagsScreenBottomSheetListsAndItems
@Composable
fun EditTagsScreenBottomSheetListsAndItems(viewModel: EditTagsViewModel) {
    val lists = viewModel.lists()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spaces.large)
            .padding(top = MaterialTheme.spaces.medium)
    ) {
        if (lists.isEmpty()) {
            if (viewModel.state.addEditMode == AddEditMode.Edit) {
                Text(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(R.string.tag_not_used_message),
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(R.string.tag_used_by_message),
            )
        }
    }
    lists.forEach { list ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(top = MaterialTheme.spaces.medium)
        ) {
            Text(
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                style = MaterialTheme.typography.bodyLarge,
                text = list.name,
            )

            val listItems = viewModel.listItems()
            listItems.forEach { item ->
                if (item.listId == list.listId) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spaces.extraLarge)
                    )
                }
            }
        }
    }
}
//endregion


//region EditTagsScreenBottomSheetName
@Composable
fun EditTagsScreenBottomSheetName(viewModel: EditTagsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AppTextField(
            value = viewModel.tagName(),
            onValueChange = viewModel::onTagNameChange,
            label = stringResource(R.string.name),
            isError = viewModel.state.tagNameInvalid,
            errorMessage = viewModel.state.tagNameInvalidMessage,
        )
    }
}
//endregion


//region EditTagsScreenContent
@Composable
fun EditTagsScreenContent(viewModel: EditTagsViewModel) {
    val lazyColumnState = rememberLazyListState()
    LazyColumn(
        state = lazyColumnState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.none),
    ) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }

        viewModel.state.tagList.forEach { item ->
            item {
                EditTagsScreenTag(item, viewModel)
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
            }
        }
    }
}
//endregion


//region EditTagsScreenTag
@Composable
fun EditTagsScreenTag(
    item: EasyListsTag,
    viewModel: EditTagsViewModel
) {
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.none)
            .combinedClickable(
                onClick = { viewModel.onTagClick(item) },
                onLongClick = { viewModel.showContextIcons(item) }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.spaces.rowHeightMedium)
                .padding(start = MaterialTheme.spaces.medium)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.spaces.medium),
            ) {
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        textDecoration = TextDecoration.None,
                    ),
                    text = item.name,
                )
                val tagListItemCount = viewModel.tagListItemCount(item)
                Text(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    text = when (tagListItemCount) {
                        0 -> "Not used"
                        1 -> "Used by $tagListItemCount list item"
                        else -> "Used by $tagListItemCount list items"
                    }
                )
            }

            EditTagsScreenTagColor(item, viewModel)

            TagCheckbox(item, viewModel)
        }
    }
}
//endregion


//region EditTagsScreenTagColor
@Composable
fun EditTagsScreenTagColor(item: EasyListsTag, viewModel: EditTagsViewModel) {
    when {
        !viewModel.state.showContextItems -> {
            Box(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spaces.large)
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(
                        color = Color(
                            item.color?.toColorInt()
                                ?: MaterialTheme.colorScheme.secondaryContainer.toHexCodeWithAlpha().toColorInt()
                        ),
                    )
                    .combinedClickable(
                        onClick = { viewModel.onTagColorChangeClicked(item) },
                        onLongClick = { }
                    )
            )
        }
    }
}
//endregion


//region TagCheckbox
@Composable
fun TagCheckbox(
    item: EasyListsTag,
    viewModel: EditTagsViewModel
) {
    val (checkedState, onStateChange) = remember { mutableStateOf(false) }

    // uncheck item when context items are not shown
    when {
        !viewModel.state.showContextItems || viewModel.state.deselectCheckboxes -> {
            onStateChange(false)
        }
    }

    when {
        viewModel.state.showContextItems -> {
            Checkbox(
                modifier = Modifier.padding(MaterialTheme.spaces.none),
                checked = checkedState,
                onCheckedChange = {
                    onStateChange(!checkedState)
                    viewModel.onTagSelectedForRemovalChanged(item.tagId)
                },
            )
        }
    }
}
//endregion


//region ConfirmRemoveTags
@Composable
fun ConfirmRemove(viewModel: EditTagsViewModel) {
    when {
        viewModel.state.showConfirmationDialog -> {
            ConfirmationDialog(
                onDismissRequest = viewModel.state.confirmationOnDismissRequest,
                onConfirmation = viewModel.state.confirmationOnConfirmation,
                dialogTitle = viewModel.state.confirmationTitle,
                dialogText = viewModel.state.confirmationMessage,
            )
        }
    }
}
//endregion


//region EditTagsScreenColorPickerBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagsScreenColorPickerBottomSheet(
    viewModel: EditTagsViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember { mutableStateOf(false) }
    val colorPickerController = rememberColorPickerController()

    when (viewModel.state.showColorPickerBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet.value = false
                viewModel.onColorPickerBottomSheetDismiss()
            },
            dragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BottomSheetDefaults.DragHandle()
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                LazyColumn(modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large)) {
                    item {
                        SectionTitle(
                            title = stringResource(R.string.choose_a_color),
                            icon = {
                                IconButton(
                                    enabled = true,
                                    onClick = {
                                        viewModel.updateTagColor()
                                    },
                                ) {
                                    Icon(
                                        imageVector = MaterialIconsCheck,
                                        contentDescription = stringResource(R.string.save_tag_color),
                                    )
                                }
                                IconButton(
                                    enabled = true,
                                    onClick = {
                                        viewModel.removeTagColor()
                                    },
                                ) {
                                    Icon(
                                        imageVector = MaterialIconsDelete,
                                        contentDescription = stringResource(R.string.remove_tag_color),
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                        )
                    }

                    item {
                        ColorPickerWheel(controller = colorPickerController, viewModel = viewModel)
                    }

                    item {
                        ColorPickerBrightnessSlider(controller = colorPickerController)
                    }

                    item {
                        ColorPickerTextFieldHexCode(controller = colorPickerController, viewModel = viewModel)
                    }

                    item {
                        ColorPickerAlphaTile(controller = colorPickerController)
                    }
                }
            }
        }
    }
}
//endregion


//region ColorPickerTextFieldHexCode
@Composable
fun ColorPickerTextFieldHexCode(
    controller: ColorPickerController,
    viewModel: EditTagsViewModel
) {
    when {
        viewModel.state.userUpdatedHexCode -> {
            val userColor = Color(
                if (viewModel.state.selectedHexCode.length == 9) viewModel.state.selectedHexCode.toColorInt()
                else Color.White.toHexCodeWithAlpha().toColorInt()
            )
            controller.selectByColor(color = userColor, fromUser = true)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
        ) { }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextField(
                modifier = Modifier.padding(MaterialTheme.spaces.medium),
                value = viewModel.state.textFieldHexCode,
                onValueChange = { viewModel.updateTextFieldHexCode(it) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None
                ),
                label = { Text(text = stringResource(R.string.rgb)) },
                singleLine = true,
                maxLines = 1,
                placeholder = {
                    Text(text = stringResource(R.string.hex_placeholder))
                },
                shape = MaterialTheme.shapes.large,
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
        ) { }
    }
}
//endregion


//region ColorPickerWheel
@Composable
fun ColorPickerWheel(
    controller: ColorPickerController,
    viewModel: EditTagsViewModel
) {
    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(10.dp),
        controller = controller,
        initialColor = Color(
            if (viewModel.state.selectedHexCode.length == 9) viewModel.state.selectedHexCode.toColorInt()
            else Color.White.toHexCodeWithAlpha().toColorInt()
        ),
        onColorChanged = {
            viewModel.updateSelectedHexCode(it.hexCode)
        },
    )
}
//endregion


//region ColorPickerBrightnessSlider
@Composable
fun ColorPickerBrightnessSlider(controller: ColorPickerController) {
    BrightnessSlider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(35.dp),
        controller = controller,
    )
}
//endregion


//region ColorPickerAlphaTile
@Composable
fun ColorPickerAlphaTile(controller: ColorPickerController) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spaces.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlphaTile(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(6.dp)),
            controller = controller
        )
    }
}
//endregion
