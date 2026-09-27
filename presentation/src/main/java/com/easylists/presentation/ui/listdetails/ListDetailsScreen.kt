package com.easylists.presentation.ui.listdetails

import android.Manifest
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.graphics.toColorInt
import coil3.Bitmap
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Size
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.FramedPhoto
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.common.getContrastColor
import com.easylists.presentation.common.toHexCodeWithAlpha
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Arrow_back
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Close_small
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.icons.Info
import com.easylists.presentation.icons.MaterialIconsBrokenImage
import com.easylists.presentation.icons.More_vert
import com.easylists.presentation.icons.Photo
import com.easylists.presentation.icons.Photo_camera
import com.easylists.presentation.icons.Settings
import com.easylists.presentation.models.Screen
import com.easylists.presentation.models.ZoomState
import com.easylists.presentation.ui.theme.spaces
import com.toxicbakery.logging.Arbor
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.pop
import java.io.File
import java.io.FileOutputStream

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
        "list item" -> viewModel.initListItemsList()
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
            onRefresh = viewModel.onPullToRefresh(),
            state = pullToRefreshState,
            modifier = Modifier.padding(innerPadding),
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
            contentDescription = stringResource(R.string.create_new_list),
            tint = MaterialTheme.colorScheme.onSurface,
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
                }?.keys?.forEach { it ->
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
                }?.keys?.forEach { it ->
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
                        }?.keys?.flatMap {
                            groupedItemList.getValue(it)
                        }

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
                        }?.keys?.forEach { it ->
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
                .padding(horizontal = MaterialTheme.spaces.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListDetailsScreenListItemPhoto(
                item = item,
                modifier = Modifier,
                viewModel = viewModel
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = MaterialTheme.spaces.medium)
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
                viewModel.state.enableTags -> {
                    ListDetailsScreenListItemTags(
                        item = item,
                        modifier = Modifier.weight(0.3f),
                        viewModel = viewModel,
                    )
                }
            }

            ListDetailsScreenListItemIcons(
                item = item,
                modifier = Modifier.weight(0.1f),
                viewModel = viewModel
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
    }
}
//endregion


//region ListDetailsScreenListItemTags
@Composable
fun ListDetailsScreenListItemTags(
    item: EasyListsListItem,
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel
) {
    Column(
        modifier = Modifier.padding(end = MaterialTheme.spaces.medium),
        horizontalAlignment = Alignment.End
    ) {
        val tags = viewModel.listItemTags(item)
        tags.take(2).forEach {
            ListItemDetailsScreenTagDot(it, modifier, viewModel)
            ListItemDetailsScreenTagPillSmall(it, modifier, viewModel)
        }
    }
}
//endregion


//region ListDetailsScreenListItemIcons
@Composable
fun ListDetailsScreenListItemPhoto(
    item: EasyListsListItem,
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel,
) {
    // TODO if state.itemPhotoUri is not null or empty, show the photo
    // TODO else show a placeholder
    if (item.photoUri.isNullOrEmpty()) {
        IconButton(onClick = { /* do nothing on click */ }) {
            Icon(
                modifier = modifier,
                imageVector = MaterialIconsBrokenImage,
                contentDescription = stringResource(R.string.view_item_details),
                tint = MaterialTheme.colorScheme.surface
            )
        }
        VerticalDivider(
            modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
        )
    } else {
        FramedPhoto(
            photoUri = item.photoUri!!,
            scale = item.photoScale.toFloat(),
            normalizedOffsetX = item.photoOffsetX.toFloat(),
            normalizedOffsetY = item.photoOffsetY.toFloat(),
            modifier = modifier.padding(vertical = MaterialTheme.spaces.none),
            zoomEnabled = false,
            onTransformChanged = { newScale, newOffsetX, newOffsetY ->
                viewModel.updatePhotoTransform(newScale, newOffsetX, newOffsetY)
            },
            targetSize = Size(150, 150),
        )
        VerticalDivider(
            modifier = Modifier
                .padding(start = MaterialTheme.spaces.small)
                .padding(vertical = MaterialTheme.spaces.none)
        )
    }
}
//endregion


//region ListDetailsScreenListItemIcons
@Composable
fun ListDetailsScreenListItemIcons(
    item: EasyListsListItem,
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel,
) {
    when {
        viewModel.state.selectedItemUid == item.uid -> {
            VerticalDivider(
                modifier = Modifier.padding(vertical = MaterialTheme.spaces.none)
            )
            IconButton(onClick = { viewModel.setShowConfirmationDialogState(true) }) {
                Icon(
                    modifier = modifier,
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
                    modifier = modifier,
                    imageVector = Info,
                    contentDescription = stringResource(R.string.view_item_details)
                )
            }
        }
    }
}
//endregion


//region ListDetailsScreenOverflowMenu
@Composable
fun ListDetailsScreenOverflowMenu(viewModel: ListDetailsViewModel) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        enabled = true,
        onClick = { expanded = !expanded },
    ) {
        Icon(
            imageVector = More_vert,
            contentDescription = stringResource(R.string.overflow_menu),
        )
    }
    DropdownMenu(
        expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text(text = stringResource(R.string.settings)) }, onClick = {
            expanded = !expanded
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
    var showBottomSheet by remember { mutableStateOf(false) }

    showBottomSheet = when (viewModel.state.showListItemBottomSheet) {
        true -> true
        false -> false
    }

    if (showBottomSheet) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = {
            showBottomSheet = false
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
                                    onClick = { viewModel.saveListItem() },
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
                        ListDetailsScreenListItemBottomSheetPhotoTitle(viewModel)
                    }

                    item {
                        ListDetailsScreenListItemBottomSheetPhoto(viewModel)
                    }

                    when {
                        viewModel.state.enableTags -> {
                            item {
                                ListDetailsScreenListItemBottomSheetTagsTitle(viewModel)
                            }

                            item {
                                ListDetailsScreenListItemBottomSheetTags(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheetTagsTitle
@Composable
fun ListDetailsScreenListItemBottomSheetTagsTitle(viewModel: ListDetailsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.spaces.large)
            .padding(horizontal = MaterialTheme.spaces.large)
    ) {
        Text(
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(R.string.tags),
        )
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
                    viewModel.state.itemNameInvalidMessage.isNotEmpty() -> Text(text = viewModel.state.itemNameInvalidMessage)

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
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState("")

    when {
        viewModel.state.categoryText.isNotEmpty() -> {
            textFieldState.setTextAndPlaceCursorAtEnd(
                viewModel.categoryFromIndex()
            )
        }
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
        expanded = expanded,
        onExpandedChange = { expanded = it },
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            when {
                viewModel.state.categoryList.isNotEmpty() -> {
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
                                expanded = false
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
        viewModel.state.easyListsTagList.isNotEmpty() -> {
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


//region ListDetailsScreenListItemBottomSheetPhotoTitle
@Composable
fun ListDetailsScreenListItemBottomSheetPhotoTitle(viewModel: ListDetailsViewModel) {
    val currentContext = LocalContext.current

    val pickImageFromAlbumLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onFinishPickingImages(currentContext, uri)
    }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isImageSaved ->
            if (isImageSaved) {
                viewModel.onCameraImageSaved(currentContext)
            } else {
                viewModel.onCameraImageSavingCanceled()
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                viewModel.onCameraPermissionGranted(currentContext)
            } else {
                viewModel.onCameraPermissionDenied()
            }
        }

    // this ensures that the camera is launched only once when the url of the temp file changes
    LaunchedEffect(key1 = viewModel.state.tempCameraFileUrl) {
        viewModel.state.tempCameraFileUrl?.let {
            cameraLauncher.launch(it)
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = MaterialTheme.spaces.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(R.string.photo),
        )

        ListDetailsScreenListItemBottomSheetCameraIcon(
            permissionLauncher = permissionLauncher,
            modifier = Modifier.weight(0.13f),
            viewModel = viewModel
        )

        ListDetailsScreenListItemBottomSheetPhotoIcon(
            pickImageFromAlbumLauncher = pickImageFromAlbumLauncher,
            modifier = Modifier.weight(0.13f),
            viewModel = viewModel
        )

        ListDetailsScreenListItemBottomSheetDeleteIcon(
            modifier = Modifier.weight(0.13f),
            viewModel = viewModel
        )
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheetCameraIcon
@Composable
fun ListDetailsScreenListItemBottomSheetCameraIcon(
    permissionLauncher: ActivityResultLauncher<String>,
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel
) {
    when {
        viewModel.state.enableCamera -> {
            IconButton(
                modifier = modifier,
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = Photo_camera,
                    contentDescription = stringResource(R.string.take_a_picture)
                )
            }
        }
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheetPhotoIcon
@Composable
fun ListDetailsScreenListItemBottomSheetPhotoIcon(
    pickImageFromAlbumLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel
) {
    IconButton(
        modifier = modifier,
        onClick = {
            pickImageFromAlbumLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    ) {
        Icon(
            modifier = Modifier,
            imageVector = Photo,
            contentDescription = stringResource(R.string.take_a_picture)
        )
    }
}
//endregion


//region ListDetailsScreenListItemBottomSheetDeleteIcon
@Composable
fun ListDetailsScreenListItemBottomSheetDeleteIcon(
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel
) {
    when {
        viewModel.state.itemPhotoUri != null -> {
            IconButton(
                modifier = modifier,
                onClick = {
                    Arbor.i("Delete photo")
                }
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = Delete,
                    contentDescription = stringResource(R.string.take_a_picture)
                )
            }
        }
    }
}
//endregion


//region Modifier.pinchToZoom, used for pinch to zoom on Add/Edit Item bottom sheet
fun Modifier.pinchToZoom(
    state: ZoomState,
    maxScale: Float = 5f,
    doubleTapScale: Float = 2.5f,
    onTap: (() -> Unit)? = null,
): Modifier = this
    .pointerInput(state) {
        awaitEachGesture {
            // Initial pass: runs before the bottom sheet / LazyColumn see the events
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val fingers = event.changes.count { it.pressed }

                if (fingers >= 2 || state.isZoomed) {
                    val zoom = if (fingers >= 2) event.calculateZoom() else 1f
                    val pan = event.calculatePan()
                    val centroid = event.calculateCentroid()

                    if (centroid.isSpecified) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val newScale = (state.scale * zoom).coerceIn(1f, maxScale)
                        val z = newScale / state.scale

                        // Keep the content point under the fingers stationary
                        val raw = (centroid - center) * (1f - z) + state.offset * z + pan

                        val maxX = size.width * (newScale - 1f) / 2f
                        val maxY = size.height * (newScale - 1f) / 2f

                        state.scale = newScale
                        state.offset = Offset(
                            raw.x.coerceIn(-maxX, maxX) / maxX,
                            raw.y.coerceIn(-maxY, maxY) / maxY,
                        )

                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                }
            } while (event.changes.any { it.pressed })
            // No reset here: the image stays zoomed after the fingers lift.
        }
    }
    .pointerInput(state, onTap) {
        detectTapGestures(
            onTap = { onTap?.invoke() },
            onDoubleTap = { tap ->
                if (state.isZoomed) {
                    state.reset()
                    // reset view model values to defaults
                } else {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxX = size.width * (doubleTapScale - 1f) / 2f
                    val maxY = size.height * (doubleTapScale - 1f) / 2f
                    val raw = (tap - center) * (1f - doubleTapScale)
                    state.scale = doubleTapScale
                    state.offset = Offset(
                        raw.x.coerceIn(-maxX, maxX) / maxX,
                        raw.y.coerceIn(-maxY, maxY) / maxY,
                    )
                }
            },
        )
    }
    // Clip to the Box's original bounds (the sheet clips anyway), OUTSIDE the layer
    .clipToBounds()
    .graphicsLayer {
        scaleX = state.scale
        scaleY = state.scale
        translationX = state.offset.x * size.width
        translationY = state.offset.y * size.height
    }
//endregion


//region ListDetailsScreenListItemBottomSheetPhoto
@Composable
fun ListDetailsScreenListItemBottomSheetPhoto(viewModel: ListDetailsViewModel) {
    when {
        viewModel.state.itemPhotoUri != null -> {
            Row(modifier = Modifier.fillMaxWidth()) {
                FramedPhoto(
                    photoUri = viewModel.state.itemPhotoUri!!,
                    scale = viewModel.state.itemPhotoScale.toFloat(),
                    normalizedOffsetX = viewModel.state.itemPhotoOffset.x,
                    normalizedOffsetY = viewModel.state.itemPhotoOffset.y,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    zoomEnabled = true,
                    onTransformChanged = { newScale, newOffsetX, newOffsetY ->
                        viewModel.updatePhotoTransform(newScale, newOffsetX, newOffsetY)
                    },
                )
            }
        }
    }



//    val zoom = remember { ZoomState(
//        initialScale = viewModel.state.itemPhotoScale.toFloat(),
//        initialOffset = viewModel.state.itemPhotoOffset
//    ) }
//
//    when {
//        viewModel.state.itemPhotoUri != null -> {
//            Row(modifier = Modifier.fillMaxWidth()) {
//                Box(
//                    modifier = Modifier
//                        .aspectRatio(1f)
//                        .fillMaxWidth()
//                        .pinchToZoom(zoom),
//                ) {
//                    AsyncImage(
//                        model = ImageRequest
//                            .Builder(LocalContext.current)
//                            .data(viewModel.state.itemPhotoUri)
//                            .build(),
//                        contentDescription = stringResource(R.string.list_item_image),
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier.fillMaxSize(),
//                    )
//
//                    viewModel.onPhotoScaleChange(zoom.scale.toDouble())
//                    viewModel.onPhotoOffsetChange(zoom.offset)
//                }
//            }
//        }
//    }
}
//endregion


//region ListDetailsScreenTagPill
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListDetailsScreenTagPill(
    easyListsTag: EasyListsTag,
    viewModel: ListDetailsViewModel,
) {
    val backgroundColor = Color(
        easyListsTag.color?.toColorInt()
            ?: if (easyListsTag.isSelected)
                MaterialTheme.colorScheme.secondaryContainer.toHexCodeWithAlpha()
                    .toColorInt()
            else MaterialTheme.colorScheme.tertiaryContainer.toHexCodeWithAlpha().toColorInt()
    )

    Box(
        modifier = Modifier
            .padding(MaterialTheme.spaces.small)
            .combinedClickable(onClick = { viewModel.onTagClick(easyListsTag) }, onLongClick = {})
            .background(
                color = backgroundColor,
                RoundedCornerShape(25.dp)
            )
            .clip(RoundedCornerShape(25.dp))
    ) {
        Row(
            modifier = Modifier.padding(
                start = MaterialTheme.spaces.medium, end = MaterialTheme.spaces.small
            ), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                color = backgroundColor.getContrastColor(),
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
                    tint = backgroundColor.getContrastColor()
                )
            }
        }
    }
}
//endregion


//region ListItemDetailsScreenTagDot
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItemDetailsScreenTagDot(
    easyListsTag: EasyListsTag,
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel,
) {
    when {
        !viewModel.state.expandTagPills -> {
            Row(
                modifier = Modifier
                    .size(20.dp)
            ) {
                Box(
                    modifier = modifier
                        .size(20.dp)
                        .height(20.dp)
                        .padding(MaterialTheme.spaces.extraSmall)
                        .combinedClickable(
                            onClick = { viewModel.expandTagPills() },
                            onLongClick = {}
                        )
                        .clip(CircleShape)
                        .background(
                            color = Color(
                                easyListsTag.color?.toColorInt()
                                    ?: MaterialTheme.colorScheme.secondaryContainer.toHexCodeWithAlpha()
                                        .toColorInt()
                            ),
                        )
                )
            }
        }
    }
}
//endregion


//region ListDetailsScreenTagPillSmall
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItemDetailsScreenTagPillSmall(
    easyListsTag: EasyListsTag,
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel,
) {
    when {
        viewModel.state.expandTagPills -> {
            val backgroundColor = Color(
                easyListsTag.color?.toColorInt()
                    ?: if (easyListsTag.isSelected)
                        MaterialTheme.colorScheme.secondaryContainer.toHexCodeWithAlpha()
                            .toColorInt()
                    else MaterialTheme.colorScheme.tertiaryContainer.toHexCodeWithAlpha().toColorInt()
            )
            Box(
                modifier = Modifier
                    .padding(MaterialTheme.spaces.extraSmall)
                    .combinedClickable(
                        onClick = { viewModel.expandTagPills() },
                        onLongClick = {}
                    )
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = MaterialTheme.spaces.medium, end = MaterialTheme.spaces.small
                    ), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        color = backgroundColor.getContrastColor(),
                        modifier = Modifier.padding(
                            start = MaterialTheme.spaces.none, end = MaterialTheme.spaces.small
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        text = easyListsTag.name
                    )
                }
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
        viewModel.state.showConfirmationDialog -> {
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
        viewModel.state.showConfirmationDialog -> {
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
