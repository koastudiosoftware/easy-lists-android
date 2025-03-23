package com.easylists.presentation.ui.listdetails

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Arrow_back
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Close_small
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
        viewModel.init(sharedViewModel.listUid, sharedViewModel.listName)
    }

    when (viewModel.state.nextDataFetchStage) {
        "category" -> viewModel.initCategoryList()
        "item" -> viewModel.initListItemsList()
        "tag" -> viewModel.initTagList()
        "tag list item" -> viewModel.initTagListItemList()
    }

    Scaffold(modifier = Modifier, snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            title = { ListDetailsScreenTitle(viewModel) },
            navigationIcon = { ListDetailsScreenTopAppBarNavigationIcon(navController) },
            actions = { ListDetailsScreenActionIcons(viewModel) },
        )
    }) { innerPadding ->

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

            ConfirmRemoveCrossedOffItems(viewModel)
            ConfirmRemoveListItem(viewModel)

            ListDetailsScreenListItemBottomSheet(viewModel)

            ListDetailsScreenContent(viewModel)

        }
    }

}


//region ListDetailsScreenTitle
@Composable
fun ListDetailsScreenTitle(viewModel: ListDetailsViewModel) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = viewModel.state.listName)
    }
}
//endregion


//region ListDetailsScreenActionIcons
@Composable
fun ListDetailsScreenActionIcons(viewModel: ListDetailsViewModel) {
    IconButton(
        onClick = { viewModel.showListItemBottomSheet() }
    ) {
        Icon(
            modifier = Modifier,
            imageVector = Add,
            contentDescription = stringResource(R.string.create_new_list)
        )
    }
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
                    groupedItemList.getValue(it).forEach {
                        item {
                            ListDetailsScreenListItem(it, viewModel)
                        }
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
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
                    groupedItemList.getValue(it).forEach {
                        item {
                            ListDetailsScreenListItem(it, viewModel)
                        }
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                        }
                    }
                }
                //endregion

                //region crossed off items
                val count = groupedItemList?.filterKeys {
                    it.first == true
                }?.count()
                when (viewModel.state.groupCrossedOffItems) {
                    GroupCrossedOffItems.AllTogether -> {
                        if (count != null && count > 0) {
                            item {
                                ListDetailsScreenCategoryTitle(
                                    stringResource(R.string.crossed_off), true
                                )
                            }
                        }

                        val crossedOffItems = groupedItemList?.filterKeys {
                            it.first == true
                        }?.keys?.map {
                            groupedItemList.getValue(it)
                        }?.flatten()

                        if (viewModel.state.sortCrossedOffItems == SortCrossedOffItems.MostRecentOnTop) {
                            crossedOffItems?.sortedByDescending {
                                it.crossedOffTimestamp
                            }?.forEach {
                                item {
                                    ListDetailsScreenListItem(it, viewModel)
                                }
                                item {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                                }
                            }
                        } else {
                            crossedOffItems?.sortedBy {
                                it.name
                            }?.forEach {
                                item {
                                    ListDetailsScreenListItem(it, viewModel)
                                }
                                item {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                                }
                            }
                        }
                        if (count != null && count > 0) {
                            item {
                                ListDetailsScreenDeleteCrossedOffItems(viewModel)
                            }
                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                            }
                        }
                    }

                    GroupCrossedOffItems.ByCategory -> {
                        //region items with a category that are crossed off
                        groupedItemList?.filterKeys {
                            it.first == true && it.second != "Uncategorized"
                        }?.keys?.forEach {
                            item {
                                ListDetailsScreenCategoryTitle(it.second.toString(), true)
                            }
                            if (viewModel.state.sortCrossedOffItems == SortCrossedOffItems.MostRecentOnTop) {
                                groupedItemList.getValue(it).sortedByDescending {
                                    it.crossedOffTimestamp
                                }.forEach {
                                    item {
                                        ListDetailsScreenListItem(it, viewModel)
                                    }
                                    item {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                                    }
                                }
                            } else {
                                groupedItemList.getValue(it).forEach {
                                    item {
                                        ListDetailsScreenListItem(it, viewModel)
                                    }
                                    item {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                                    }
                                }
                            }
                        }
                        //endregion

                        //region uncategorized items that are crossed off
                        groupedItemList?.filterKeys {
                            it.first == true && it.second == "Uncategorized"
                        }?.keys?.forEach {
                            item {
                                ListDetailsScreenCategoryTitle(it.second.toString(), true)
                            }
                            if (viewModel.state.sortCrossedOffItems == SortCrossedOffItems.MostRecentOnTop) {
                                groupedItemList.getValue(it).sortedByDescending {
                                    it.crossedOffTimestamp
                                }.forEach {
                                    item {
                                        ListDetailsScreenListItem(it, viewModel)
                                    }
                                    item {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                                    }
                                }
                            } else {
                                groupedItemList.getValue(it).forEach {
                                    item {
                                        ListDetailsScreenListItem(it, viewModel)
                                    }
                                    item {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                                    }
                                }
                            }
                        }
                        //endregion

                        if (count != null && count > 0) {
                            item {
                                ListDetailsScreenDeleteCrossedOffItems(viewModel)
                            }
                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
                            }
                        }
                    }
                }
                //endregion
            }
        }
    }
}
//endregion


//region ListDetailsScreenDeleteCrossedOffItems
@Composable
fun ListDetailsScreenDeleteCrossedOffItems(viewModel: ListDetailsViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = MaterialTheme.spaces.large)
            .clickable(onClick = { viewModel.setShowConfirmationDialogState(true) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            text = stringResource(R.string.delete_all_crossed_off_items),
        )
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
                horizontal = MaterialTheme.spaces.large, vertical = MaterialTheme.spaces.medium
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            text = title.uppercase(),
        )
    }
}
//endregion


//region ListDetailsScreenListItem
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListDetailsScreenListItem(
    item: EasyListsListItem, viewModel: ListDetailsViewModel
) {
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.none)
            .combinedClickable(
                onClick = { viewModel.onListItemClick(item) },
                onLongClick = { viewModel.showContextIcons(item) }),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.spaces.rowHeightMedium)
                .padding(start = MaterialTheme.spaces.large)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(end = MaterialTheme.spaces.medium)
            ) {
                var text = item.name
                if (item.quantity != null) text += " (${item.quantity})"
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        textDecoration = if (item.crossedOff == true) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    text = text,
                )
                when {
                    item.notes?.isNotEmpty() == true -> {
                        Text(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            text = item.notes!!,
                        )
                    }
                }
            }
            when {
                viewModel.state.selectedItemUid == item.uid -> {
                    VerticalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
                    )
                    IconButton(onClick = { viewModel.setShowConfirmationDialogState(true) }) {
                        Icon(
                            modifier = Modifier.weight(0.1f),
                            imageVector = Delete,
                            contentDescription = stringResource(R.string.remove_item)
                        )
                    }
                }

                else -> {
                    VerticalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
                    )
                    IconButton(onClick = {
                        viewModel.onListItemInfoClick(item, AddEditMode.Edit)
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
        expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
        DropdownMenuItem(text = { Text(text = stringResource(R.string.settings)) }, onClick = {
            expanded.value = !expanded.value
//                viewModel.showExportDataBottomSheet()
        }, leadingIcon = {
            Icon(
                Settings, contentDescription = "Localized description"
            )
        })
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsScreenListItemBottomSheet(viewModel: ListDetailsViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet = remember { mutableStateOf(false) }

    when (viewModel.state.showListItemBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = {
            showBottomSheet.value = false
            viewModel.onItemBottomSheetDismiss()
        }, dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
            }
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                LazyColumn(modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large)) {
                    item {
                        SectionTitle(
                            title = stringResource(
                                if (viewModel.state.addEditMode == AddEditMode.Add) R.string.add_item
                                else R.string.edit_item
                            ), icon = {
                                IconButton(
                                    enabled = viewModel.listItemIconButtonEnabled(),
                                    onClick = { viewModel.addListItem() },
                                ) {
                                    Icon(
                                        imageVector = Check,
                                        contentDescription = stringResource(R.string.add_list_item),
                                    )
                                }
                            }, modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
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

                    item {
                        ListDetailsScreenListItemBottomSheetTags(viewModel)
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
            keyboardOptions = KeyboardOptions(
                capitalization = viewModel.state.capitalization.keyboardCapitalization,
                keyboardType = KeyboardType.Text,
                showKeyboardOnFocus = true,
            ),
            isError = viewModel.state.itemNameInvalid,
            supportingText = {
                when {
                    viewModel.state.itemNameInvalidMessage.isNotEmpty() == true -> Text(text = viewModel.state.itemNameInvalidMessage)

                    else -> null
                }
            })
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
            keyboardOptions = KeyboardOptions(
                capitalization = viewModel.state.capitalization.keyboardCapitalization,
                keyboardType = KeyboardType.Text,
                showKeyboardOnFocus = true,
            ),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                showKeyboardOnFocus = true,
            ),
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

    when {
        viewModel.state.categoryText.isNotEmpty() == true -> {
            textFieldState.setTextAndPlaceCursorAtEnd(
                viewModel.categoryFromIndex()
            )
        }
    }

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
            onValueChange = { viewModel.onCategoryChange(it) },
            readOnly = false,
            value = viewModel.state.categoryText,
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


//region ListDetailsScreenListItemBottomSheetTags
@Composable
fun ListDetailsScreenListItemBottomSheetTags(viewModel: ListDetailsViewModel) {
    when {
        viewModel.state.easyListsTagList.isNotEmpty() == true -> {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spaces.medium)
            ) {
                val boxWithConstraintsScope = this
                var widthConsumed: Dp = MaterialTheme.spaces.none

                var nextIndex = 0
                var lastIndex = -1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.spaces.none)
                ) {
                    while (lastIndex < viewModel.state.easyListsTagList.size - 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spaces.none)
                        ) {
                            run breaking@{
                                viewModel.state.easyListsTagList.forEachIndexed { index, tag ->
                                    lastIndex = index

                                    // skip items already added to previous row(s)
                                    if (index < nextIndex) return@forEachIndexed

                                    // calculate width of tag pill
                                    var width = measureTextWidth(
                                        tag.name, MaterialTheme.typography.bodyMedium
                                    ) + (MaterialTheme.spaces.small * 2) + (MaterialTheme.spaces.medium * 2)
                                    if (tag.isSelected) {
                                        width += 16.dp
                                    }

                                    if (widthConsumed + width > boxWithConstraintsScope.maxWidth) {
                                        // reduce last index by 1 as we didn't actually display the last item
                                        lastIndex = index - 1
                                        nextIndex = index
                                        widthConsumed = MaterialTheme.spaces.none

                                        // break here because we have to start a new row
                                        return@breaking
                                    }

                                    ListDetailsScreenTagPill(
                                        easyListsTag = tag, viewModel = viewModel
                                    )

                                    widthConsumed += width
                                    lastIndex = index
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
//endregion


//region ListDetailsScreenTagPill
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListDetailsScreenTagPill(
    easyListsTag: EasyListsTag,
    viewModel: ListDetailsViewModel,
) {
    Box(
        modifier = Modifier
            .padding(MaterialTheme.spaces.small)
            .combinedClickable(onClick = { viewModel.onTagClick(easyListsTag) }, onLongClick = {})
            .background(
                color = if (easyListsTag.isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(25.dp)
            )
            .clip(RoundedCornerShape(25.dp))) {
        Row(
            modifier = Modifier.padding(
                start = MaterialTheme.spaces.medium, end = MaterialTheme.spaces.small
            ), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                color = if (easyListsTag.isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(
                    start = MaterialTheme.spaces.none, end = MaterialTheme.spaces.small
                ),
                style = MaterialTheme.typography.bodyMedium,
                text = easyListsTag.name
            )
            if (easyListsTag.isSelected) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = MaterialTheme.spaces.none),
                    imageVector = Close_small,
                    contentDescription = stringResource(R.string.create_new_list),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
//endregion


//region measureTextWidth
@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}
//endregion


//region ListDetailsScreenTopAppBarNavigationIcon
@Composable
fun ListDetailsScreenTopAppBarNavigationIcon(navController: NavController<Screen>) {
    IconButton(
        onClick = { navController.pop() }) {
        Icon(
            painter = rememberVectorPainter(Arrow_back),
            contentDescription = stringResource(R.string.return_to_previous_screen),
            modifier = Modifier.padding(start = MaterialTheme.spaces.mediumLarge),
        )
    }
}
//endregion


//region ConfirmRemoveCrossedOffItems
@Composable
fun ConfirmRemoveCrossedOffItems(viewModel: ListDetailsViewModel) {
    when {
        viewModel.state.showConfirmationDialog == true -> {
            ConfirmationDialog(
                onDismissRequest = {
                    viewModel.setShowConfirmationDialogState(false)
                },
                onConfirmation = {
                    viewModel.deleteAllCrossedOffItems()
                    viewModel.setShowConfirmationDialogState(false)
                },
                dialogTitle = stringResource(R.string.confirm_removal),
                dialogText = stringResource(R.string.remove_crossed_off_items_warning),
            )
        }
    }
}
//endregion


//region ConfirmRemoveListItem
@Composable
fun ConfirmRemoveListItem(viewModel: ListDetailsViewModel) {
    when {
        viewModel.state.showConfirmationDialog == true -> {
            ConfirmationDialog(
                onDismissRequest = {
                    viewModel.setShowConfirmationDialogState(false)
                },
                onConfirmation = {
                    viewModel.removeListItem()
                    viewModel.setShowConfirmationDialogState(false)
                },
                dialogTitle = stringResource(R.string.confirm_removal),
                dialogText = stringResource(R.string.remove_list_items_warning),
            )
        }
    }
}
//endregion
