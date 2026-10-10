package com.easylists.presentation.ui.lists

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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.ListsAction
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.AppTextField
import com.easylists.presentation.common.composables.ScreenLoading
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsCategory
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.MaterialIconsDelete
import com.easylists.presentation.icons.MaterialIconsEdit
import com.easylists.presentation.icons.MaterialIconsMoreVert
import com.easylists.presentation.icons.MaterialIconsSettings
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


//region ListsScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    navController: NavController<Screen>,
    sharedViewModel: SharedViewModel,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val interaction = viewModel.interaction
    val editor = viewModel.editor

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { ListsScreenTitle() },
                actions = {
                    ListsScreenActionIcons(
                        navController = navController,
                        onAddClick = { viewModel.onActionButtonClick(ListsAction.Add) },
                    )
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

                is ListListUiState.Success -> ListsScreenContent(
                    lists = state.lists,
                    selectedListId = interaction.selectedListId,
                    onListClick = { list ->
                        sharedViewModel.listUid = list.listId.toString()
                        sharedViewModel.listName = list.name
                        navController.navigate(Screen.ListDetails)
                    },
                    onListLongClick = viewModel::showContextIcons,
                    onListEdit = viewModel::onListEditButtonClick,
                    onListDelete = { viewModel.setShowConfirmationDialogState(true) },
                )
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

        ListsScreenListBottomSheet(
            editor = editor,
            validation = validation,
            onNameChange = viewModel::onListNameChange,
            onNotesChange = viewModel::onListNotesChange,
            onSave = viewModel::addList,
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


//region ListsScreenActionIcons
@Composable
fun ListsScreenActionIcons(
    navController: NavController<Screen>,
    onAddClick: () -> Unit,
) {
    IconButton(onClick = onAddClick) {
        Icon(
            modifier = Modifier,
            imageVector = MaterialIconsAdd,
            contentDescription = stringResource(R.string.create_new_list),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
    ListsScreenOverflowMenu(navController)
}
//endregion


//region ListItem
@Composable
fun ListItem(
    list: EasyListsList,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
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

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        IconButton(
            modifier = Modifier.weight(0.12f),
            onClick = if (isSelected) onDelete else onEdit,
        ) {
            Icon(
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
                imageVector = if (isSelected) MaterialIconsDelete else MaterialIconsEdit,
                contentDescription = stringResource(
                    if (isSelected) R.string.delete_list else R.string.edit_list
                ),
            )
        }
    }
    HorizontalDivider()
}
//endregion


//region ListsScreenContent
@Composable
fun ListsScreenContent(
    lists: List<EasyListsList>,
    selectedListId: String?,
    onListClick: (EasyListsList) -> Unit,
    onListLongClick: (EasyListsList) -> Unit,
    onListEdit: (EasyListsList) -> Unit,
    onListDelete: () -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.none)) {
        item { HorizontalDivider() }

        if (lists.isEmpty()) {
            item {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(R.string.no_lists_found),
                )
            }
        } else {
            items(items = lists, key = { it.listId.toString() }) { list ->
                ListItem(
                    list = list,
                    isSelected = selectedListId == list.listId,
                    onClick = { onListClick(list) },
                    onLongClick = { onListLongClick(list) },
                    onEdit = { onListEdit(list) },
                    onDelete = onListDelete,
                )
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
                    text = stringResource(R.string.edit_categories),
                )
            },
            onClick = {
                expanded.value = !expanded.value
                navController.navigate(Screen.EditCategories)
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
                    text = stringResource(R.string.edit_tags),
                )
            },
            onClick = {
                expanded.value = !expanded.value
                navController.navigate(Screen.EditTags)
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
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: (onSaved: () -> Unit) -> Unit,
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
                            IconButton(
                                enabled = validation.canSave,
                                onClick = { onSave(hideAndDismiss) }
                            ) {
                                Icon(
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    imageVector = MaterialIconsCheck,
                                    contentDescription = stringResource(R.string.add_list),
                                )
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
