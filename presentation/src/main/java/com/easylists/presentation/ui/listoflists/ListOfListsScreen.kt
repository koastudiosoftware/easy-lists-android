package com.easylists.presentation.ui.listoflists

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easylists.domain.models.EasyListsList
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.ListOfListsAction
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.icons.Edit
import com.easylists.presentation.icons.Info
import com.easylists.presentation.icons.More_vert
import com.easylists.presentation.icons.Settings
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.SolarizedRed
import com.easylists.presentation.ui.theme.spaces
import com.toxicbakery.logging.Arbor
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfListsScreen(
    navController: NavController<Screen>,
    sharedViewModel: SharedViewModel,
    viewModel: ListOfListsViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { ListOfListsScreenTitle() },
                actions = { ListOfListsScreenActionIcons(navController, viewModel) },
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

            ListOfListsScreenAddListBottomSheet(viewModel)

            ListOfListsScreenContent(navController, viewModel, sharedViewModel)

        }
    }

}


//region ListOfListsScreenTitle
@Composable
fun ListOfListsScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.app_name))
    }
}
//endregion


//region ListOfListsScreenActionIcons
@Composable
fun ListOfListsScreenActionIcons(
    navController: NavController<Screen>,
    viewModel: ListOfListsViewModel
) {
    IconButton(onClick = {
        viewModel.onActionButtonClick(ListOfListsAction.Add)
    }) {
        Icon(
            modifier = Modifier,
            imageVector = Add,
            contentDescription = stringResource(R.string.create_new_list)
        )
    }
    ListOfListsScreenOverflowMenu(navController, viewModel)
}
//endregion


//region MasterListItem
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MasterListItem(
    item: EasyListsList,
    viewModel: ListOfListsViewModel,
    sharedViewModel: SharedViewModel,
    navController: NavController<Screen>,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.none)
            .height(64.dp)
            .combinedClickable(
                onClick = {
                    sharedViewModel.listUid = item.uid.toString()
                    navController.navigate(Screen.ListDetails)
                },
                onLongClick = { viewModel.showContextIcons(item) }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .padding(
                horizontal = MaterialTheme.spaces.large,
                vertical = MaterialTheme.spaces.medium,
            )
        ) {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                text = item.name
            )
            Arbor.i("item.notes: ${item.notes}")
            when {
                item.notes?.isNotEmpty() == true -> {
                    Text(
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        text = item.notes!!
                    )
                }
            }
        }

        when {
            viewModel.state.selectedListUid == item.uid -> {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .weight(0.33f)
                    .padding(
                        horizontal = MaterialTheme.spaces.small,
                        vertical = MaterialTheme.spaces.medium,
                    )
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        VerticalDivider(
                            modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
                        )
                        IconButton(
                            onClick = {
//                                 viewModel.onActionButtonClick(ListOfListsAction.Add)
                            }
                        ) {
                            Icon(
                                modifier = Modifier,
                                imageVector = Edit,
                                contentDescription = stringResource(R.string.create_new_list)
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
                        )
                        IconButton(
                            onClick = { viewModel.removeList() }
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

    }
    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
}
//endregion


//region ListOfListsScreenContent
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListOfListsScreenContent(
    navController: NavController<Screen>,
    viewModel: ListOfListsViewModel,
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
//                itemsIndexed(viewModel.state.listList ?: emptyList()) { _, item ->
//                    Row(
//                        modifier = Modifier
//                            .padding(horizontal = MaterialTheme.spaces.none)
//                            .combinedClickable(
//                                onClick = {
//                                    sharedViewModel.listUid = item.uid.toString()
//                                    navController.navigate(Screen.ListDetails)
//                                },
//                                onLongClick = { viewModel.showContextIcons(item) }
//                            ),
//                    ) {
//                        Row(
//                            modifier = Modifier
//                            .fillMaxWidth()
//                            .height(52.dp)
//                            .padding(horizontal = MaterialTheme.spaces.large)
//                            .padding(
//                                top = MaterialTheme.spaces.medium,
//                                bottom = MaterialTheme.spaces.large
//                            ),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Arbor.i("item: $item")
//                            Column(modifier = Modifier.fillMaxSize().weight(1f)) {
//                                Text(
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis,
//                                    style = MaterialTheme.typography.bodyLarge,
//                                    text = item.name
//                                )
//                                when {
//                                    item.notes?.isNotEmpty() == true -> {
//                                        Arbor.i("item.notes: ${item.notes}")
//                                        Text(
//                                            color = SolarizedRed,
//                                            maxLines = 1,
//                                            overflow = TextOverflow.Ellipsis,
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            text = item.notes ?: "null notes"
//                                        )
//                                    }
//                                }
//                            }
//                            when {
//                                viewModel.state.selectedListUid == item.uid -> {
//                                    IconButton(
//                                        modifier = Modifier.weight(0.1f),
//                                        onClick = {
////                                          viewModel.onActionButtonClick(ListOfListsAction.Add)
//                                        }
//                                    ) {
//                                        Icon(
//                                            modifier = Modifier,
//                                            imageVector = Edit,
//                                            contentDescription = stringResource(R.string.create_new_list)
//                                        )
//                                    }
//                                    IconButton(
//                                        modifier = Modifier.weight(0.1f),
//                                        onClick = { viewModel.removeList() }
//                                    ) {
//                                        Icon(
//                                            modifier = Modifier,
//                                            imageVector = Delete,
//                                            contentDescription = stringResource(R.string.create_new_list)
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
//                }
            }
            else -> {
//                item {
//                    Text(
//                        style = MaterialTheme.typography.titleLarge,
//                        text = stringResource(R.string.no_lists_found)
//                    )
//                }
            }
        }
    }
}
//endregion


//region ListOfListsScreenOverflowMenu
@Composable
fun ListOfListsScreenOverflowMenu(
    navController: NavController<Screen>,
    viewModel: ListOfListsViewModel
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


//region ListOfListsScreenAddListBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfListsScreenAddListBottomSheet(viewModel: ListOfListsViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet = remember { mutableStateOf(false) }

    when (viewModel.state.showAddListBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet.value = false
                viewModel.onAddListBottomSheetDismiss()
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
                            title = stringResource(R.string.add_list),
                            icon = {
                                IconButton(
                                    enabled = viewModel.addListIconButtonEnabled(),
                                    onClick = { viewModel.addList()
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
                        ListOfListsScreenAddListBottomSheetListName(viewModel)
                    }

                    item {
                        ListOfListsScreenAddListBottomSheetListNotes(viewModel)
                    }

                }
            }
        }
    }
}
//endregion


//region ListOfListsScreenAddListBottomSheetListName
@Composable
fun ListOfListsScreenAddListBottomSheetListName(viewModel: ListOfListsViewModel) {
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


//region ListOfListsScreenAddListBottomSheetListNotes
@Composable
fun ListOfListsScreenAddListBottomSheetListNotes(viewModel: ListOfListsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
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
