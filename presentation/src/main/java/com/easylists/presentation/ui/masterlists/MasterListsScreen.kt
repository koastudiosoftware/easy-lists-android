package com.easylists.presentation.ui.masterlists

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.MasterListsAction
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.icons.Info
import com.easylists.presentation.icons.More_vert
import com.easylists.presentation.icons.Settings
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
    viewModel: MasterListsViewModel = hiltViewModel()
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
            onRefresh = viewModel.onPullToRefresh(true),
            state = pullToRefreshState,
            modifier = Modifier.padding(innerPadding),
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = viewModel.state.isPullToRefreshing,
                    state = pullToRefreshState
                )
            },
        ) {

            ConfirmRemoveList(viewModel)

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
    viewModel: MasterListsViewModel
) {
    IconButton(onClick = {
        viewModel.onActionButtonClick(MasterListsAction.Add)
    }) {
        Icon(
            modifier = Modifier,
            imageVector = Add,
            contentDescription = stringResource(R.string.create_new_list)
        )
    }
    MasterListsScreenOverflowMenu(navController, viewModel)
}
//endregion


//region MasterListItem
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MasterListItem(
    list: EasyListsList,
    viewModel: MasterListsViewModel,
    sharedViewModel: SharedViewModel,
    navController: NavController<Screen>,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.none)
            .height(64.dp)
            .combinedClickable(
                onClick = {
                    sharedViewModel.listUid = list.uid.toString()
                    sharedViewModel.listName = list.name.toString()
                    navController.navigate(Screen.ListDetails)
                },
                onLongClick = { viewModel.showContextIcons(list) }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(
                    horizontal = MaterialTheme.spaces.large,
                    vertical = MaterialTheme.spaces.medium,
                ),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                text = list.name
            )
            when {
                list.notes?.isNotEmpty() == true -> {
                    Text(
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        text = list.notes!!
                    )
                }
            }
        }
        VerticalDivider(
            modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
        )
        IconButton(
            modifier = Modifier.weight(0.16f),
            onClick = {
                viewModel.onListEditButtonClick(list = list)
            }
        ) {
            Icon(
                modifier = Modifier,
                imageVector = Info,
                contentDescription = stringResource(R.string.create_new_list)
            )
        }

        when {
            viewModel.state.selectedListUid == list.uid -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.22f)
                        .padding(
                            horizontal = MaterialTheme.spaces.medium,
                            vertical = MaterialTheme.spaces.medium,
                        )
                ) {
                    VerticalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
                    )
                    IconButton(
                        onClick = { viewModel.setShowConfirmationDialogState(true) }
                    ) {
                        Icon(
                            modifier = Modifier,
                            imageVector = Delete,
                            contentDescription = stringResource(R.string.create_new_list)
                        )
                    }
                }
            }
        }

    }
    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
}
//endregion


//region MasterListsScreenContent
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MasterListsScreenContent(
    navController: NavController<Screen>,
    viewModel: MasterListsViewModel,
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
        when (viewModel.state.listList?.isNotEmpty()) {
            true -> {
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
    viewModel: MasterListsViewModel
) {
    var expanded = remember { mutableStateOf(false) }

    IconButton(
        enabled = true,
        onClick = { expanded.value = !expanded.value },
    ) {
        Icon(
            imageVector = More_vert,
            contentDescription = stringResource(R.string.overflow_menu),
        )
    }
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.settings)) },
            onClick = {
                expanded.value = !expanded.value
                navController.navigate(Screen.Settings)
            },
            leadingIcon = {
                Icon(
                    Settings,
                    contentDescription = "Localized description"
                )
            }
        )
    }
}
//endregion


//region MasterListsScreenListBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterListsScreenListBottomSheet(viewModel: MasterListsViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet = remember { mutableStateOf(false) }

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
                                        imageVector = Check,
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
fun MasterListsScreenListBottomSheetListName(viewModel: MasterListsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.listName(),
            onValueChange = { viewModel.onListNameChange(it) },
            label = { Text(text = stringResource(R.string.name)) },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = viewModel.state.listNameInvalid,
            supportingText = {
                when {
                    viewModel.state.listNameInvalidMessage.isNotEmpty() == true ->
                        Text(text = viewModel.state.listNameInvalidMessage)

                    else -> null
                }
            }
        )
    }
}
//endregion


//region MasterListsScreenListBottomSheetListNotes
@Composable
fun MasterListsScreenListBottomSheetListNotes(viewModel: MasterListsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.listNotes(),
            onValueChange = { viewModel.onListNotesChange(it) },
            label = { Text(text = stringResource(R.string.notes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
    }
}
//endregion


//region ConfirmRemoveList
@Composable
fun ConfirmRemoveList(viewModel: MasterListsViewModel) {
    when {
        viewModel.state.showConfirmationDialog == true -> {
            ConfirmationDialog(
                onDismissRequest = {
                    viewModel.setShowConfirmationDialogState(false)
                },
                onConfirmation = {
                    viewModel.removeList()
                    viewModel.setShowConfirmationDialogState(false)
                },
                dialogTitle = stringResource(R.string.confirm_removal),
                dialogText = stringResource(R.string.remove_list_warning),
            )
        }
    }
}
//endregion