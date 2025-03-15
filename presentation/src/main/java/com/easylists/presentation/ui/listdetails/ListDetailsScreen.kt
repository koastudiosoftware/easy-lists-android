package com.easylists.presentation.ui.listdetails

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.R
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Arrow_back
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.icons.Info
import com.easylists.presentation.icons.More_vert
import com.easylists.presentation.icons.Settings
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
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

    when (viewModel.state.nextDataFetchStage) {
        "category" -> {
            viewModel.initCategoryList()
        }

        "item" -> {
            viewModel.initListItemsList()
        }
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

            ListDetailsScreenAddListItemBottomSheet(viewModel)

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
        viewModel.showAddListItemBottomSheet()
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
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.none),
    ) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }

        when {
            viewModel.state.groupedItemList != null -> {
                val groupedItemList = viewModel.state.groupedItemList

                //region items with a category that are not crossed off
                groupedItemList?.filterKeys {
                    it.first == false && it.second != "Uncategorized"
                }?.keys?.forEach {
                    item {
                        ListDetailsScreenCategoryTitle(it.second.toString())
                    }
                    item {
                        groupedItemList.getValue(it).forEach {
                            ListDetailsScreenListItem(it, viewModel)
                        }
                    }
                }
                //endregion

                //region uncategorized items that are not crossed off
                groupedItemList?.filterKeys {
                    it.first == false && it.second == "Uncategorized"
                }?.keys?.forEach {
                    item {
                        ListDetailsScreenCategoryTitle(it.second.toString())
                    }
                    item {
                        groupedItemList.getValue(it).forEach {
                            ListDetailsScreenListItem(it, viewModel)
                        }
                    }
                }
                //endregion

                //region crossed off items
                when (viewModel.state.groupCrossedOffItems) {
                    GroupCrossedOffItems.AllTogether -> {
                        val count = groupedItemList?.filterKeys {
                            it.first == true
                        }?.count()
                        if (count != null && count > 0) {
                            item {
                                ListDetailsScreenCategoryTitle(
                                    stringResource(R.string.crossed_off),
                                    true
                                )
                            }
                        }
                        groupedItemList?.filterKeys {
                            it.first == true
                        }?.keys?.forEach {
                            item {
                                groupedItemList.getValue(it).forEach {
                                    ListDetailsScreenListItem(it, viewModel)
                                }
                            }
                        }
                    }

                    GroupCrossedOffItems.ByCategory -> {
                        //region items with a category that are not crossed off
                        groupedItemList?.filterKeys {
                            it.first == true && it.second != "Uncategorized"
                        }?.keys?.forEach {
                            item {
                                ListDetailsScreenCategoryTitle(it.second.toString(), true)
                            }
                            item {
                                groupedItemList.getValue(it).forEach {
                                    ListDetailsScreenListItem(it, viewModel)
                                }
                            }
                        }
                        //endregion

                        //region uncategorized items that are not crossed off
                        groupedItemList?.filterKeys {
                            it.first == true && it.second == "Uncategorized"
                        }?.keys?.forEach {
                            item {
                                ListDetailsScreenCategoryTitle(it.second.toString(), true)
                            }
                            item {
                                groupedItemList.getValue(it).forEach {
                                    ListDetailsScreenListItem(it, viewModel)
                                }
                            }
                        }
                        //endregion
                    }
                }
                //endregion
            }
        }
    }
}
//endregion


//region ListDetailsScreenCategoryTitle
@Composable
fun ListDetailsScreenCategoryTitle(title: String, crossedOff: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spaces.none)
            .background(
                if (crossedOff) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spaces.large,
                vertical = MaterialTheme.spaces.medium
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
            text = title.uppercase(),
        )
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
                onClick = { viewModel.onListItemClick(item) },
                onLongClick = { viewModel.showContextIcons(item) }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(start = MaterialTheme.spaces.large)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var text = item.name
            if (item.quantity != null)
                text += " (${item.quantity})"
            Text(
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(textDecoration = if (item.crossedOff == true) TextDecoration.LineThrough else TextDecoration.None),
                text = text,
            )
            when {
                viewModel.state.selectedItemUid == item.uid -> {
                    VerticalDivider(
                        modifier = Modifier
                            .padding(vertical = MaterialTheme.spaces.none)
                    )
                    IconButton(onClick = { viewModel.removeListItem() }) {
                        Icon(
                            modifier = Modifier.weight(0.1f),
                            imageVector = Delete,
                            contentDescription = stringResource(R.string.remove_item)
                        )
                    }
                }

                else -> {
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
                }
            }
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

    when (viewModel.state.showAddListItemBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet.value = false
                viewModel.onAddItemBottomSheetDismiss()
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
                            title = stringResource(R.string.add_item),
                            icon = {
                                IconButton(
                                    enabled = viewModel.addListItemIconButtonEnabled(),
                                    onClick = { viewModel.addListItem() },
                                ) {
                                    Icon(
                                        imageVector = Check,
                                        contentDescription = stringResource(R.string.add_list_item),
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                        )
                    }

                    item {
                        ListDetailsScreenListItemBottomSheetName(viewModel)
                    }

                    item {
                        ListDetailsScreenListItemBottomSheetQuantity(viewModel)
                    }

                    item {
                        ListDetailsScreenListItemBottomSheetCategory(viewModel)
                    }

                    item {
                        ListDetailsScreenListItemBottomSheetNotes(viewModel)
                    }

                }
            }
        }
    }
}
//endregion


//region ListDetailsScreenListBottomSheetListItemName
@Composable
fun ListDetailsScreenListItemBottomSheetName(viewModel: ListDetailsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.itemName(),
            onValueChange = { viewModel.onItemNameChange(it) },
            label = { Text(text = stringResource(R.string.name)) },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = viewModel.state.itemNameInvalid,
            supportingText = {
                when {
                    viewModel.state.itemNameInvalidMessage.isNotEmpty() == true ->
                        Text(text = viewModel.state.itemNameInvalidMessage)

                    else -> null
                }
            }
        )
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheetListItemNotes
@Composable
fun ListDetailsScreenListItemBottomSheetNotes(viewModel: ListDetailsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.itemNotes(),
            onValueChange = { viewModel.onItemNotesChange(it) },
            label = { Text(text = stringResource(R.string.notes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheetQuantity
@Composable
fun ListDetailsScreenListItemBottomSheetQuantity(viewModel: ListDetailsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium),
            value = viewModel.itemQuantity(),
            onValueChange = { viewModel.onItemQuantityChange(it) },
            label = { Text(text = stringResource(R.string.quantity)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheetCategory
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsScreenListItemBottomSheetCategory(viewModel: ListDetailsViewModel) {
    var expanded = remember { mutableStateOf(false) }
    var textFieldState = rememberTextFieldState("")

//    when {
//        viewModel.state.eventTypeListUiData?.isNotEmpty() == true -> {
//            textFieldState.setTextAndPlaceCursorAtEnd(
//                viewModel.eventTypeNameFromIndex()
//            )
//        }
//    }

    ExposedDropdownMenuBox(
        modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it },
    ) {
        TextField(
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text(text = stringResource(R.string.category)) },
            onValueChange = { /* do nothing here, look at ExposedDropdownMenu below */ },
            readOnly = true,
            value = textFieldState.text.toString(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
        )
        ExposedDropdownMenu(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            when {
                viewModel.state.categoryList.isNotEmpty() == true -> {
                    viewModel.state.categoryList.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            onClick = {
                                textFieldState.setTextAndPlaceCursorAtEnd(option.name)
                                expanded.value = false
                                viewModel.onCategoryChange(index)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
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