package com.easylists.presentation.ui.listdetails

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.R
import com.easylists.presentation.common.ListOfListsAction
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Arrow_back
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.icons.Edit
import com.easylists.presentation.icons.Info
import com.easylists.presentation.icons.More_vert
import com.easylists.presentation.icons.Settings
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import com.toxicbakery.logging.Arbor
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsScreen(
    navController: NavController<Screen>,
    sharedViewModel: SharedViewModel,
    viewModel: ListDetailsViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = null) {
        viewModel.init(sharedViewModel.listUid)
    }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { ListDetailsScreenTitle() },
                navigationIcon = { ListDetailsScreenTopAppBarNavigationIcon(navController) },
                actions = { ListDetailsScreenActionIcons(viewModel) },
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

//            ListOfListsScreenAddListBottomSheet(viewModel)

            ListDetailsScreenContent(viewModel)

        }
    }

}


//region ListDetailsScreenTitle
@Composable
fun ListDetailsScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // TODO this needs to be changed to show the list name
        Text(text = stringResource(R.string.app_name))
    }
}
//endregion


//region ListDetailsScreenActionIcons
@Composable
fun ListDetailsScreenActionIcons(viewModel: ListDetailsViewModel) {
    IconButton(onClick = {
//        viewModel.onActionButtonClick(ListOfListsAction.Add)
    }) {
        Icon(
            modifier = Modifier,
            imageVector = Add,
            contentDescription = stringResource(R.string.create_new_list)
        )
    }
//    ListDetailsScreenOverflowMenu(viewModel)
}
//endregion


//region ListDetailsScreenContent
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListDetailsScreenContent(viewModel: ListDetailsViewModel) {
    val lazyColumnState = rememberLazyListState()
    LazyColumn(
        state = lazyColumnState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.medium),
    ) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }

        itemsIndexed(viewModel.state.listItemList) { _, item ->
            ListDetailsScreenListItem(item, viewModel)
        }
    }
}
//endregion


//region ListDetailsScreenListItem
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListDetailsScreenListItem(
    item: EasyListsListItem,
    viewModel: ListDetailsViewModel
) {
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.none)
            .combinedClickable(
                onClick = {
                    Arbor.i("Clicked ${item.name}")
                },
                onLongClick = {
                    Arbor.i("Long clicked ${item.name}")
                }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(
                    top = MaterialTheme.spaces.medium,
                    bottom = MaterialTheme.spaces.large
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                text = item.name
            )
            VerticalDivider(
                modifier = Modifier
                    .padding(vertical = MaterialTheme.spaces.none)
            )
            IconButton(onClick = {
            }) {
                Icon(
                    modifier = Modifier.weight(0.1f),
                    imageVector = Info,
                    contentDescription = stringResource(R.string.view_item_details)
                )
            }
//          style = TextStyle(textDecoration = TextDecoration.LineThrough)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
    }
}
//endregion


//region ListDetailsScreenOverflowMenu
@Composable
fun ListDetailsScreenOverflowMenu(viewModel: ListDetailsViewModel) {
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
//                viewModel.showExportDataBottomSheet()
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
fun ListDetailsScreenAddListItemBottomSheet(viewModel: ListDetailsViewModel) {
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
//                                    enabled = viewModel.addListIconButtonEnabled(),
                                    onClick = {
//                                        viewModel.addList()
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
                        ListDetailsScreenListBottomSheetListItemName(viewModel)
                    }

                    item {
                        ListDetailsScreenListItemBottomSheetListItemNotes(viewModel)
                    }

                }
            }
        }
    }
}
//endregion


//region ListDetailsScreenListBottomSheetListItemName
@Composable
fun ListDetailsScreenListBottomSheetListItemName(viewModel: ListDetailsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.listItemName(),
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


//region ListDetailsScreenListItemBottomSheetListItemNotes
@Composable
fun ListDetailsScreenListItemBottomSheetListItemNotes(viewModel: ListDetailsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.listItemNotes(),
            onValueChange = { viewModel.onListNotesChange(it) },
            label = { Text(text = stringResource(R.string.notes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
    }
}
//endregion


//region ListDetailsScreenTopAppBarNavigationIcon
@Composable
fun ListDetailsScreenTopAppBarNavigationIcon(navController: NavController<Screen>) {
    IconButton(
        onClick = { navController.pop() }
    ) {
        Icon(
            painter = rememberVectorPainter(Arrow_back),
            contentDescription = stringResource(R.string.return_to_previous_screen),
            modifier = Modifier.padding(start = MaterialTheme.spaces.mediumLarge),
        )
    }
}
//endregion