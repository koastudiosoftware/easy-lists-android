package com.easylists.presentation.ui.lists

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.MasterListsAction
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsCategory
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.MaterialIconsDelete
import com.easylists.presentation.icons.MaterialIconsEdit
import com.easylists.presentation.icons.MaterialIconsInfo
import com.easylists.presentation.icons.MaterialIconsMoreVert
import com.easylists.presentation.icons.MaterialIconsSettings
import com.easylists.presentation.icons.MaterialIconsTag
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.navigate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterListsScreen(
    navController: NavController<Screen>,
    sharedViewModel: SharedViewModel,
    viewModel: ListsViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { MasterListsScreenTitle() },
                actions = { MasterListsScreenActionIcons(navController, viewModel) },
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

            ConfirmDeleteList(viewModel)

            MasterListsScreenListBottomSheet(viewModel)

            MasterListsScreenContent(navController, viewModel, sharedViewModel)
        }
    }
}


//region MasterListsScreenTitle
@Composable
fun MasterListsScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.app_name))
    }
}
//endregion


//region MasterListsScreenActionIcons
@Composable
fun MasterListsScreenActionIcons(
    navController: NavController<Screen>,
    viewModel: ListsViewModel
) {
    IconButton(onClick = {
        viewModel.onActionButtonClick(MasterListsAction.Add)
    }) {
        Icon(
            modifier = Modifier,
            imageVector = MaterialIconsAdd,
            contentDescription = stringResource(R.string.create_new_list),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
    MasterListsScreenOverflowMenu(navController, viewModel)
}
//endregion


//region MasterListItem
@Composable
fun MasterListItem(
    list: EasyListsList,
    viewModel: ListsViewModel,
    sharedViewModel: SharedViewModel,
    navController: NavController<Screen>,
) {
    when {
        !list.isDeleted -> {
            Row(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spaces.none)
                    .combinedClickable(
                        onClick = {
                            sharedViewModel.listUid = list.listId.toString()
                            sharedViewModel.listName = list.name
                            navController.navigate(Screen.ListDetails)
                        },
                        onLongClick = { viewModel.showContextIcons(list) }
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MaterialTheme.spaces.rowHeightMedium)
                        .padding(start = MaterialTheme.spaces.large)
                        .padding(vertical = MaterialTheme.spaces.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                        when {
                            list.notes?.isNotEmpty() == true -> {
                                Text(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = list.notes!!
                                )
                            }
                        }
                    }

                    when {
                        viewModel.state.selectedListUid == list.listId -> {
                            VerticalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
                            )
                            IconButton(
                                modifier = Modifier.weight(0.12f),
                                onClick = { viewModel.setShowConfirmationDialogState(true) }
                            ) {
                                Icon(
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp),
                                    imageVector = MaterialIconsDelete,
                                    contentDescription = stringResource(R.string.create_new_list),
                                )
                            }
                        }

                        else -> {
                            VerticalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
                            )
                            IconButton(
                                modifier = Modifier.weight(0.12f),
                                onClick = {
                                    viewModel.onListEditButtonClick(list = list)
                                }
                            ) {
                                Icon(
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp),
                                    imageVector = MaterialIconsEdit,
                                    contentDescription = stringResource(R.string.create_new_list)
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(
                modifier =
                    Modifier.padding(vertical = MaterialTheme.spaces.none)
            )
        }
    }
}
//endregion


//region MasterListsScreenContent
@Composable
fun MasterListsScreenContent(
    navController: NavController<Screen>,
    viewModel: ListsViewModel,
    sharedViewModel: SharedViewModel
) {
    val lazyColumnState = rememberLazyListState()
    LazyColumn(
        state = lazyColumnState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.none),
    ) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }
        when {
            viewModel.state.listList?.isNotEmpty() == true -> {
                val masterList = viewModel.state.listList
                masterList?.forEach { item ->
                    item {
                        MasterListItem(item, viewModel, sharedViewModel, navController)
                    }
                }
            }

            else -> {
                item {
                    Text(
                        style = MaterialTheme.typography.titleLarge,
                        text = stringResource(R.string.no_lists_found)
                    )
                }
            }
        }
    }
}
//endregion


//region MasterListsScreenOverflowMenu
@Composable
fun MasterListsScreenOverflowMenu(
    navController: NavController<Screen>,
    viewModel: ListsViewModel
) {
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


//region MasterListsScreenListBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterListsScreenListBottomSheet(viewModel: ListsViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember { mutableStateOf(false) }

    when (viewModel.state.showListBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet.value = false
                viewModel.onListBottomSheetDismiss()
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
                                if (viewModel.state.addEditMode == AddEditMode.Add) R.string.add_list
                                else R.string.edit_list
                            ),
                            icon = {
                                IconButton(
                                    enabled = viewModel.listIconButtonEnabled(),
                                    onClick = {
                                        viewModel.addList()
                                    },
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
                        MasterListsScreenListBottomSheetListName(viewModel)
                    }

                    item {
                        MasterListsScreenListBottomSheetListNotes(viewModel)
                    }
                }
            }
        }
    }
}
//endregion


//region MasterListsScreenListBottomSheetListName
@Composable
fun MasterListsScreenListBottomSheetListName(viewModel: ListsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium),
            value = viewModel.listName(),
            onValueChange = { viewModel.onListNameChange(it) },
            label = { Text(text = stringResource(R.string.name)) },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                capitalization = viewModel.state.capitalization.keyboardCapitalization,
                keyboardType = KeyboardType.Text,
                showKeyboardOnFocus = true,
            ),
            isError = viewModel.state.listNameInvalid,
            supportingText = {
                when {
                    viewModel.state.listNameInvalidMessage.isNotEmpty() ->
                        Text(text = viewModel.state.listNameInvalidMessage)
                }
            }
        )
    }
}
//endregion


//region MasterListsScreenListBottomSheetListNotes
@Composable
fun MasterListsScreenListBottomSheetListNotes(viewModel: ListsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium),
            value = viewModel.listNotes(),
            onValueChange = { viewModel.onListNotesChange(it) },
            label = { Text(text = stringResource(R.string.notes)) },
            keyboardOptions = KeyboardOptions(
                capitalization = viewModel.state.capitalization.keyboardCapitalization,
                keyboardType = KeyboardType.Text,
                showKeyboardOnFocus = true,
            ),
        )
    }
}
//endregion


//region ConfirmDeleteList
@Composable
fun ConfirmDeleteList(viewModel: ListsViewModel) {
    when {
        viewModel.state.showConfirmationDialog -> {
            ConfirmationDialog(
                onDismissRequest = {
                    viewModel.setShowConfirmationDialogState(false)
                },
                onConfirmation = {
                    viewModel.deleteList()
                    viewModel.setShowConfirmationDialogState(false)
                },
                dialogTitle = stringResource(R.string.confirm_deletion),
                dialogText = stringResource(R.string.delete_list_warning),
            )
        }
    }
}
//endregion
