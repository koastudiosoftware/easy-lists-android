package com.easylists.presentation.ui.edittags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.EasyListsTag
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.LocalCapitalization
import com.easylists.presentation.common.composables.AppTextField
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsCancel
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.MaterialIconsDelete
import com.easylists.presentation.models.ColorEditorState
import com.easylists.presentation.models.EditTagsState
import com.easylists.presentation.models.TagPendingDelete
import com.easylists.presentation.models.Screen
import com.easylists.presentation.models.TagSheetState
import com.easylists.presentation.models.usageFor
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

    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }

    // one-shot messages from the ViewModel
    val message = state.messageRes?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    // back leaves selection mode first, then the screen
    BackHandler(enabled = state.selectionMode, onBack = viewModel::exitSelectionMode)

    CompositionLocalProvider(LocalCapitalization provides s.capitalization) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.edit_tags)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.pop() }) {
                            Icon(
                                imageVector = MaterialIconsArrowBack,
                                contentDescription = stringResource(R.string.return_to_previous_screen),
                            )
                        }
                    },
                    actions = { EditTagsActions(state, viewModel) },
                )
            }
        ) { innerPadding ->
            EditTagsList(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding),
            )
        }

        state.tagSheet?.let { TagEditorSheet(sheet = it, state = state, viewModel = viewModel) }
        state.colorEditor?.let { ColorPickerSheet(editor = it, viewModel = viewModel) }
        state.pendingDelete?.let { ConfirmDeleteDialog(pending = it, viewModel = viewModel) }
    }
}


//region EditTagsActions
@Composable
private fun EditTagsActions(state: EditTagsState, viewModel: EditTagsViewModel) {
    if (state.selectionMode) {
        IconButton(
            enabled = state.selectedTagIds.isNotEmpty(),
            onClick = viewModel::requestDeleteSelected,
        ) {
            Icon(
                imageVector = MaterialIconsDelete,
                contentDescription = stringResource(R.string.delete_selected_tags),
            )
        }
        IconButton(onClick = viewModel::exitSelectionMode) {
            Icon(
                imageVector = MaterialIconsCancel,
                contentDescription = stringResource(R.string.cancel_deletion_of_selected_tags),
            )
        }
    } else {
        IconButton(onClick = viewModel::onAddTagClick) {
            Icon(
                imageVector = MaterialIconsAdd,
                contentDescription = stringResource(R.string.create_new_tag),
            )
        }
    }
}
//endregion


//region EditTagsList
@Composable
private fun EditTagsList(
    state: EditTagsState,
    viewModel: EditTagsViewModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { HorizontalDivider() }

        items(items = state.tagList, key = { it.tagId ?: it.name }) { tag ->
            val tagId = tag.tagId
            Column {
                TagRow(
                    tag = tag,
                    usageCount = tagId?.let { state.tagUsageCounts[it] } ?: 0,
                    selectionMode = state.selectionMode,
                    selected = tagId != null && tagId in state.selectedTagIds,
                    onClick = { viewModel.onTagClick(tag) },
                    onLongClick = { viewModel.onTagLongClick(tag) },
                    onColorClick = { viewModel.onTagColorClick(tag) },
                    onToggleSelected = { tagId?.let(viewModel::toggleSelection) },
                )
                HorizontalDivider()
            }
        }
    }
}
//endregion


//region TagRow
@Composable
private fun TagRow(
    tag: EasyListsTag,
    usageCount: Int,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onColorClick: () -> Unit,
    onToggleSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.spaces.rowHeightMedium)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = MaterialTheme.spaces.medium)
            .padding(vertical = MaterialTheme.spaces.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MaterialTheme.spaces.medium),
        ) {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                text = tag.name,
            )
            Text(
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                text = if (usageCount == 0) {
                    stringResource(R.string.tag_not_used)
                } else {
                    pluralStringResource(R.plurals.tag_used_by_list_items, usageCount, usageCount)
                },
            )
        }

        if (selectionMode) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggleSelected() },
            )
        } else {
            val swatchColor = remember(tag.color) { parseColorOrNull(tag.color) }
                ?: MaterialTheme.colorScheme.secondaryContainer
            Box(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spaces.large)
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(color = swatchColor)
                    .clickable(onClick = onColorClick)
            )
        }
    }
}
//endregion


//region ConfirmDeleteDialog
@Composable
private fun ConfirmDeleteDialog(pending: TagPendingDelete, viewModel: EditTagsViewModel) {
    ConfirmationDialog(
        onDismissRequest = viewModel::onDeleteDismissed,
        onConfirmation = viewModel::onDeleteConfirmed,
        dialogTitle = stringResource(R.string.confirm_deletion),
        dialogText = stringResource(
            if (pending is TagPendingDelete.Single) R.string.delete_tag_warning
            else R.string.delete_tags_warning
        ),
    )
}
//endregion


//region SheetDragHandle
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetDragHandle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomSheetDefaults.DragHandle()
    }
}
//endregion


//region TagEditorSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagEditorSheet(
    sheet: TagSheetState,
    state: EditTagsState,
    viewModel: EditTagsViewModel,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val usage = remember(sheet.tagId, state.tagListItemList, state.listItemList, state.listList) {
        state.usageFor(sheet.tagId)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = viewModel::onTagSheetDismiss,
        dragHandle = { SheetDragHandle() },
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
                            if (sheet.mode == AddEditMode.Add) R.string.add_tag else R.string.edit_tag
                        ),
                        icon = {
                            IconButton(
                                enabled = state.canSaveTag,
                                onClick = viewModel::saveTag,
                            ) {
                                Icon(
                                    imageVector = MaterialIconsCheck,
                                    contentDescription = stringResource(R.string.save_tag),
                                )
                            }
                            if (sheet.mode == AddEditMode.Edit) {
                                IconButton(onClick = { viewModel.requestDeleteTag(sheet.tagId) }) {
                                    Icon(
                                        imageVector = MaterialIconsDelete,
                                        contentDescription = stringResource(R.string.delete_tag),
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                    )
                }

                item {
                    val duplicate = state.isTagNameDuplicate
                    AppTextField(
                        value = sheet.name,
                        onValueChange = viewModel::onTagNameChange,
                        label = stringResource(R.string.name),
                        isError = duplicate,
                        errorMessage = if (duplicate) stringResource(R.string.tag_name_in_use) else "",
                    )
                }

                if (usage.isEmpty()) {
                    if (sheet.mode == AddEditMode.Edit) {
                        item {
                            Text(
                                modifier = Modifier
                                    .padding(top = MaterialTheme.spaces.medium)
                                    .padding(horizontal = MaterialTheme.spaces.medium),
                                style = MaterialTheme.typography.bodyLarge,
                                text = stringResource(R.string.tag_not_used_message),
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            modifier = Modifier
                                .padding(top = MaterialTheme.spaces.medium)
                                .padding(horizontal = MaterialTheme.spaces.medium),
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(R.string.tag_used_by_message),
                        )
                    }
                    items(usage) { entry ->
                        Column(modifier = Modifier.padding(top = MaterialTheme.spaces.medium)) {
                            Text(
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                                style = MaterialTheme.typography.bodyLarge,
                                text = entry.list.name,
                            )
                            entry.items.forEach { listItem ->
                                Text(
                                    text = listItem.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = MaterialTheme.spaces.extraLarge)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
//endregion


//region ColorPickerSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerSheet(
    editor: ColorEditorState,
    viewModel: EditTagsViewModel,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val controller = rememberColorPickerController()

    // captured once when the sheet opens; later wheel changes must not re-seed the picker
    val initialColor = remember { parseColorOrNull(editor.hexCode) ?: Color.White }

    // push a color typed in the hex field into the wheel, only when a full value was entered
    LaunchedEffect(editor.hexSyncCount) {
        if (editor.hexSyncCount > 0) {
            parseColorOrNull(editor.hexCode)?.let { controller.selectByColor(it, fromUser = true) }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = viewModel::onColorEditorDismiss,
        dragHandle = { SheetDragHandle() },
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
                            IconButton(onClick = viewModel::saveTagColor) {
                                Icon(
                                    imageVector = MaterialIconsCheck,
                                    contentDescription = stringResource(R.string.save_tag_color),
                                )
                            }
                            IconButton(onClick = viewModel::removeTagColor) {
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
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .padding(10.dp),
                        controller = controller,
                        initialColor = initialColor,
                        onColorChanged = { envelope ->
                            viewModel.onWheelColorChanged(envelope.hexCode)
                        },
                    )
                }

                item { ColorPickerBrightnessSlider(controller = controller) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .padding(MaterialTheme.spaces.medium),
                            value = editor.hexInput,
                            onValueChange = viewModel::onHexInputChanged,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done,
                            ),
                            label = { Text(text = stringResource(R.string.rgb)) },
                            prefix = { Text(text = "#") },
                            placeholder = { Text(text = stringResource(R.string.hex_placeholder)) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                        )
                    }
                }

                item { ColorPickerAlphaTile(controller = controller) }
            }
        }
    }
}
//endregion


//region ColorPickerBrightnessSlider
@Composable
private fun ColorPickerBrightnessSlider(controller: ColorPickerController) {
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
private fun ColorPickerAlphaTile(controller: ColorPickerController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.spaces.medium),
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


/** "#AARRGGBB" -> Color, or null when missing/malformed (a bad stored value must not crash the row). */
private fun parseColorOrNull(hex: String?): Color? =
    hex?.let { runCatching { Color(it.toColorInt()) }.getOrNull() }