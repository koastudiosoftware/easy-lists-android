package com.easylists.presentation.ui.lists

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.common.ListSortMode
import com.easylists.domain.common.ViewMode
import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.ListsAction
import com.easylists.presentation.common.composables.AppTextField
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.ScreenLoading
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.common.selectableCardBorder
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsCategory
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.MaterialIconsDelete
import com.easylists.presentation.icons.MaterialIconsDragHandle
import com.easylists.presentation.icons.MaterialIconsEdit
import com.easylists.presentation.icons.MaterialIconsMoreVert
import com.easylists.presentation.icons.MaterialIconsSettings
import com.easylists.presentation.icons.MaterialIconsSort
import com.easylists.presentation.icons.MaterialIconsTag
import com.easylists.presentation.models.ListEditorState
import com.easylists.presentation.models.ListEditorValidation
import com.easylists.presentation.models.ListListUiState
import com.easylists.presentation.models.ListNameError
import com.easylists.presentation.models.Screen
import com.easylists.presentation.models.validate
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.navigate
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


//region ListsScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    navController: NavController<Screen>,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val editor = viewModel.editor

    val interaction = viewModel.interaction
    val isRearranging = interaction.isRearranging

    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = isRearranging) { viewModel.onRearrangeDone() }  // androidx.activity.compose

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isRearranging) Text(stringResource(R.string.rearrange_lists))
                    else ListsScreenTitle()
                },
                actions = {
                    if (isRearranging) {
                        IconButton(onClick = viewModel::onRearrangeDone) {
                            Icon(
                                imageVector = MaterialIconsCheck,
                                contentDescription = stringResource(R.string.done),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else {
                        ListsScreenActionIcons(
                            navController = navController,
                            sortMode = sortMode,
                            onAddClick = { viewModel.onActionButtonClick(ListsAction.Add) },
                            onSortModeChange = viewModel::onSortModeChange,
                        )
                    }
                },
            )
        }
    ) { innerPadding ->

        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = interaction.isRefreshing,
            onRefresh = viewModel::onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.padding(innerPadding),
        ) {
            when (val state = lists) {
                ListListUiState.Loading -> ScreenLoading()

                is ListListUiState.Error -> ScreenError(
                    message = state.message,
                    onRetry = viewModel::onRefresh,
                )

                is ListListUiState.Success -> when (val mode = viewMode) {
                    null -> ScreenLoading()
                    else -> ListsScreenContent(
                        viewMode = mode,
                        lists = state.lists,
                        onListClick = { list ->
                            navController.navigate(
                                Screen.ListItems(list.listId.toString(), list.name)
                            )
                        },
                        onListEdit = viewModel::onListEditButtonClick,
                        isRearranging = isRearranging,
                        onListsReordered = viewModel::onListsReordered,
                    )
                }
            }
        }
    }

    // dialogs and bottom sheets: they don't need to live inside the PullToRefreshBox
    if (interaction.showConfirmationDialog) {
        ConfirmationDialog(
            onDismissRequest = { viewModel.setShowConfirmationDialogState(false) },
            onConfirmation = {
                viewModel.deleteList()
                viewModel.setShowConfirmationDialogState(false)
            },
            dialogTitle = stringResource(R.string.confirm_deletion),
            dialogText = stringResource(R.string.delete_list_warning),
        )
    }

    if (editor != null) {
        val existing = (lists as? ListListUiState.Success)?.lists.orEmpty()
        val validation = remember(editor, existing) { editor.validate(existing) }
        val hasChanges = remember(editor, existing) {
            if (editor.mode == AddEditMode.Add) {
                true
            } else {
                val original = existing.find { it.listId.toString() == editor.listId }
                original != null && (
                        editor.name.trim() != original.name ||
                                editor.notes != original.notes.orEmpty()
                        )
            }
        }

        ListsScreenListBottomSheet(
            editor = editor,
            validation = validation,
            hasChanges = hasChanges,
            onNameChange = viewModel::onListNameChange,
            onNotesChange = viewModel::onListNotesChange,
            onSave = viewModel::addList,
            onDelete = { viewModel.setShowConfirmationDialogState(true) },
            onDismiss = viewModel::onListBottomSheetDismiss,
        )
    }
}
//endregion


//region ListsScreenTitle
@Composable
fun ListsScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.app_name))
    }
}
//endregion


//region RearrangeHandle
@Composable
private fun RearrangeHandle(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = MaterialIconsDragHandle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}
//endregion


//region ListsScreenActionIcons
@Composable
fun ListsScreenActionIcons(
    navController: NavController<Screen>,
    sortMode: ListSortMode,
    onAddClick: () -> Unit,
    onSortModeChange: (ListSortMode) -> Unit,
) {
    IconButton(onClick = onAddClick) {
        Icon(
            modifier = Modifier,
            imageVector = MaterialIconsAdd,
            contentDescription = stringResource(R.string.create_new_list),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
    ListsScreenSortMenu(sortMode, onSortModeChange)
    ListsScreenOverflowMenu(navController)
}
//endregion


//region ListsScreenSortMenu
@Composable
fun ListsScreenSortMenu(
    sortMode: ListSortMode,
    onSortModeChange: (ListSortMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = MaterialIconsSort,
                contentDescription = stringResource(R.string.sort_lists),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ListSortMode.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(option.labelRes()),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSortModeChange(option)
                    },
                    trailingIcon = {
                        if (option == sortMode) {
                            Icon(
                                imageVector = MaterialIconsCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }
    }
}
//endregion


//region ListSortMode.labelRes
private fun ListSortMode.labelRes(): Int = when (this) {
    ListSortMode.NameAsc -> R.string.sort_name_asc
    ListSortMode.NameDesc -> R.string.sort_name_desc
    ListSortMode.Manual -> R.string.sort_manual
}
//endregion


//region ListCard
@Composable
fun ListCard(
    list: EasyListsList,
    isRearranging: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = selectableCardBorder(false),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isRearranging, onClick = onClick)
                .padding(start = MaterialTheme.spaces.large)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = MaterialTheme.spaces.medium),
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    text = list.name,
                )
                list.notes?.takeIf { it.isNotEmpty() }?.let { notes ->
                    Text(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        text = notes,
                    )
                }
            }

            if (isRearranging) {
                RearrangeHandle()
            } else {
                IconButton(onClick = onEdit) {
                    Icon(
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                        imageVector = MaterialIconsEdit,
                        contentDescription = stringResource(R.string.edit_list),
                    )
                }
            }
        }
    }
}
//endregion


//region ListItem
@Composable
fun ListItem(
    list: EasyListsList,
    isRearranging: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
) {
    // Column because ReorderableItem hosts its content in a Box, which would
    // otherwise stack the row and the divider on top of each other
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation)
            .background(
                if (elevation > 0.dp) MaterialTheme.colorScheme.background
                else Color.Transparent
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isRearranging, onClick = onClick)
                .height(MaterialTheme.spaces.rowHeightMedium)
                .padding(start = MaterialTheme.spaces.large)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = MaterialTheme.spaces.medium),
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        textDecoration = TextDecoration.None,
                    ),
                    text = list.name,
                )
                list.notes?.takeIf { it.isNotEmpty() }?.let { notes ->
                    Text(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        text = notes,
                    )
                }
            }

            if (isRearranging) {
                RearrangeHandle(modifier = Modifier.weight(0.12f))
            } else {
                IconButton(modifier = Modifier.weight(0.12f), onClick = onEdit) {
                    Icon(
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                        imageVector = MaterialIconsEdit,
                        contentDescription = stringResource(R.string.edit_list),
                    )
                }
            }
        }
        HorizontalDivider()
    }
}
//endregion


//region ListsScreenContent
@Composable
fun ListsScreenContent(
    viewMode: ViewMode,
    isRearranging: Boolean,
    lists: List<EasyListsList>,
    onListClick: (EasyListsList) -> Unit,
    onListEdit: (EasyListsList) -> Unit,
    onListsReordered: (List<String>) -> Unit,
) {
    val isCard = viewMode == ViewMode.Card
    val lazyListState = rememberLazyListState()

    // Local copy so rows follow the finger during a drag; the DB is written once, on drop.
    // Resets whenever the data layer emits a different list.
    var displayed by remember(lists) { mutableStateOf(lists) }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // by key, not index, because the header items shift the indexes
        val fromIndex = displayed.indexOfFirst { it.listId.toString() == from.key }
        val toIndex = displayed.indexOfFirst { it.listId.toString() == to.key }
        if (fromIndex >= 0 && toIndex >= 0) {
            displayed = displayed.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = if (isCard) {
            PaddingValues(
                horizontal = MaterialTheme.spaces.large,
                vertical = MaterialTheme.spaces.medium,
            )
        } else PaddingValues(),
        verticalArrangement = Arrangement.spacedBy(
            if (isCard) MaterialTheme.spaces.medium else MaterialTheme.spaces.none
        ),
    ) {
        if (!isCard) item { HorizontalDivider() }

        if (displayed.isEmpty()) {
            item {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(R.string.no_lists_found),
                )
            }
        } else {
            items(items = displayed, key = { it.listId.toString() }) { list ->
                ReorderableItem(
                    state = reorderableState,
                    key = list.listId.toString(),
                    enabled = isRearranging,
                ) { isDragging ->
                    val elevation by animateDpAsState(
                        targetValue = when {
                            isDragging -> 16.dp
                            isRearranging -> 2.dp
                            else -> 0.dp
                        },
                        label = "dragElevation",
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isDragging && isCard) 1.03f else 1f,
                        label = "dragScale",
                    )
                    val haptic = LocalHapticFeedback.current

                    val dragModifier = if (isRearranging) {
                        Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragStopped = {
                                    onListsReordered(displayed.map { it.listId.toString() })
                                },
                            )
                    } else Modifier

                    when (viewMode) {
                        ViewMode.Card -> ListCard(
                            modifier = dragModifier,
                            list = list,
                            isRearranging = isRearranging,
                            elevation = elevation,
                            onClick = { onListClick(list) },
                            onEdit = { onListEdit(list) },
                        )
                        ViewMode.List -> ListItem(
                            modifier = dragModifier,
                            list = list,
                            isRearranging = isRearranging,
                            elevation = elevation,
                            onClick = { onListClick(list) },
                            onEdit = { onListEdit(list) },
                        )
                    }
                }
            }
        }
    }
}
//endregion


//region ListsScreenOverflowMenu
@Composable
fun ListsScreenOverflowMenu(navController: NavController<Screen>) {
    val expanded = remember { mutableStateOf(false) }

    IconButton(
        enabled = true,
        onClick = { expanded.value = !expanded.value },
    ) {
        Icon(
            imageVector = MaterialIconsMoreVert,
            contentDescription = stringResource(R.string.overflow_menu),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = stringResource(R.string.categories),
                )
            },
            onClick = {
                expanded.value = !expanded.value
                navController.navigate(Screen.Categories)
            },
            leadingIcon = {
                Icon(
                    MaterialIconsCategory,
                    contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = stringResource(R.string.tags),
                )
            },
            onClick = {
                expanded.value = !expanded.value
                navController.navigate(Screen.Tags)
            },
            leadingIcon = {
                Icon(
                    MaterialIconsTag,
                    contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = stringResource(R.string.settings),
                )
            },
            onClick = {
                expanded.value = !expanded.value
                navController.navigate(Screen.Settings)
            },
            leadingIcon = {
                Icon(
                    MaterialIconsSettings,
                    contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        )
    }
}
//endregion


//region ListsScreenListBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreenListBottomSheet(
    editor: ListEditorState,
    validation: ListEditorValidation,
    hasChanges: Boolean,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: (onSaved: () -> Unit) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val hideAndDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
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
                            if (editor.mode == AddEditMode.Add) R.string.add_list
                            else R.string.edit_list
                        ),
                        icon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    enabled = validation.canSave && hasChanges,
                                    onClick = { onSave(hideAndDismiss) }
                                ) {
                                    Icon(
                                        tint = if (hasChanges)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.outline,
                                        imageVector = MaterialIconsCheck,
                                        contentDescription = stringResource(R.string.add_list),
                                    )
                                }
                                if (editor.mode != AddEditMode.Add) {
                                    IconButton(onClick = onDelete) {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            imageVector = MaterialIconsDelete,
                                            contentDescription = stringResource(R.string.delete_list),
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                    )
                }

                item {
                    AppTextField(
                        value = editor.name,
                        onValueChange = onNameChange,
                        label = stringResource(R.string.name),
                        isError = validation.nameError != null,
                        errorMessage = if (validation.nameError == ListNameError.Duplicate)
                            stringResource(R.string.list_name_in_use) else "",
                    )
                }

                item {
                    AppTextField(
                        value = editor.notes,
                        onValueChange = onNotesChange,
                        label = stringResource(R.string.notes),
                        singleLine = false,
                    )
                }
            }
        }
    }
}
//endregion


//region Loading / error states
@Composable
fun ScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ScreenError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spaces.large),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry) { Text(text = stringResource(R.string.retry)) }
    }
}
//endregion