package com.easylists.presentation.ui.listitems

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.common.AppSettings
import com.easylists.domain.common.ViewMode
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.FramedPhoto
import com.easylists.presentation.common.composables.AppTextField
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.common.getContrastColor
import com.easylists.presentation.common.selectableCardBorder
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsBrokenImage
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.MaterialIconsClose
import com.easylists.presentation.icons.MaterialIconsDelete
import com.easylists.presentation.icons.MaterialIconsEdit
import com.easylists.presentation.icons.MaterialIconsInsertPhoto
import com.easylists.presentation.icons.MaterialIconsPhotoCamera
import com.easylists.presentation.models.ItemEditorState
import com.easylists.presentation.models.ItemEditorValidation
import com.easylists.presentation.models.ItemNameError
import com.easylists.presentation.models.ListItemsUiState
import com.easylists.presentation.models.ListRow
import com.easylists.presentation.models.Screen
import com.easylists.presentation.models.validate
import com.easylists.presentation.ui.lists.ScreenError
import com.easylists.presentation.ui.lists.ScreenLoading
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItemsScreen(
    navController: NavController<Screen>,
    listId: String,
    listName: String,
    viewModel: ListItemsViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.init(listId, listName)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interaction = viewModel.interaction
    val editor = viewModel.editor
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = viewModel.listName) },
                navigationIcon = { ListItemsTopAppBarNavigationIcon(navController) },
                actions = {
                    IconButton(onClick = viewModel::onAddItemClick) {
                        Icon(
                            imageVector = MaterialIconsAdd,
                            contentDescription = stringResource(R.string.add_item),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        }
    ) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = interaction.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.padding(innerPadding),
        ) {
            when (val state = uiState) {
                ListItemsUiState.Loading -> ScreenLoading()

                is ListItemsUiState.Error -> ScreenError(
                    message = state.message,
                    onRetry = viewModel::onRefresh,
                )

                is ListItemsUiState.Success -> ListItemsScreenContent(
                    state = state,
                    expandTagPills = interaction.expandTagPills,
                    onItemClick = viewModel::onListItemClick,
                    onItemEdit = viewModel::onEditItemClick,
                    onTagPillsClick = viewModel::onToggleTagPills,
                    onDeleteCrossedOff = { viewModel.setShowDeleteCrossedOffDialog(true) },
                )
            }
        }
    }

    if (interaction.showDeleteCrossedOffDialog) {
        ConfirmationDialog(
            onDismissRequest = { viewModel.setShowDeleteCrossedOffDialog(false) },
            onConfirmation = {
                viewModel.deleteAllCrossedOffItems()
                viewModel.setShowDeleteCrossedOffDialog(false)
            },
            dialogTitle = stringResource(R.string.confirm_deletion),
            dialogText = stringResource(R.string.delete_crossed_off_items_warning),
        )
    }

    if (interaction.showDeleteItemDialog) {
        ConfirmationDialog(
            onDismissRequest = { viewModel.setShowDeleteItemDialog(false) },
            onConfirmation = {
                viewModel.deleteListItem()
                viewModel.setShowDeleteItemDialog(false)
            },
            dialogTitle = stringResource(R.string.confirm_deletion),
            dialogText = stringResource(R.string.delete_list_items_warning),
        )
    }

    val success = uiState as? ListItemsUiState.Success
    if (editor != null && success != null) {
        val validation = remember(editor, success.items) { editor.validate(success.items) }
        val hasChanges = remember(editor, success) { editor.hasChanges(success) }
        val uncategorizedLabel = stringResource(R.string.uncategorized)
        val actions = remember(viewModel) {
            ItemEditorActions(
                onNameChange = viewModel::onItemNameChange,
                onQuantityChange = viewModel::onItemQuantityChange,
                onNotesChange = viewModel::onItemNotesChange,
                onCategoryChange = viewModel::onCategoryChange,
                onTagClick = viewModel::onTagClick,
                onPhotoChosen = viewModel::onPhotoChosen,
                onPhotoTransformChanged = viewModel::onPhotoTransformChanged,
                onPhotoRemove = viewModel::onPhotoRemove,
                onSave = { viewModel.saveListItem(uncategorizedLabel, it) },
                onDelete = { viewModel.setShowDeleteItemDialog(true) },
                onDismiss = viewModel::onEditorDismiss,
            )
        }

        ListItemsBottomSheet(
            editor = editor,
            validation = validation,
            hasChanges = hasChanges,
            categories = success.categories,
            tags = success.tags,
            settings = success.settings,
            actions = actions,
        )
    }
}


//region hasChanges
// true when the editor differs from what is saved; always true when adding
private fun ItemEditorState.hasChanges(state: ListItemsUiState.Success): Boolean {
    val orig = this.original ?: return true

    val originalCategory = orig.categoryId
        ?.let { id -> state.categories.find { it.categoryId == id }?.name }
        .orEmpty()
    val originalTagIds = state.tagLinks
        .filter { it.listItemId == itemId }
        .map { it.tagId }
        .toSet()
    val originalPhotoUri = orig.photoUri?.takeIf { it.isNotEmpty() }

    return name.trim() != orig.name ||
            quantity != orig.quantity?.toString().orEmpty() ||
            notes != orig.notes.orEmpty() ||
            !categoryName.trim().equals(originalCategory, ignoreCase = true) ||
            selectedTagIds != originalTagIds ||
            photo?.uri != originalPhotoUri ||
            photo?.let { it.current != it.initial } == true
}
//endregion


//region ListItemsTopAppBarNavigationIcon
@Composable
fun ListItemsTopAppBarNavigationIcon(navController: NavController<Screen>) {
    IconButton(onClick = { navController.pop() }) {
        Icon(
            painter = rememberVectorPainter(MaterialIconsArrowBack),
            contentDescription = stringResource(R.string.return_to_previous_screen),
        )
    }
}
//endregion


//region ListItemsScreenContent
@Composable
fun ListItemsScreenContent(
    state: ListItemsUiState.Success,
    expandTagPills: Boolean,
    onItemClick: (EasyListsListItem) -> Unit,
    onItemEdit: (EasyListsListItem) -> Unit,
    onTagPillsClick: () -> Unit,
    onDeleteCrossedOff: () -> Unit,
) {
    val isCard = state.settings.viewMode == ViewMode.Card

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.none),
    ) {
        if (!isCard) item { HorizontalDivider() }

        items(items = state.rows, key = { it.key }) { row ->
            when (row) {
                is ListRow.CategoryHeader -> ListItemsCategoryTitle(
                    title = row.categoryName ?: stringResource(R.string.uncategorized),
                    crossedOff = row.crossedOff,
                    isCard = isCard,
                )

                ListRow.CrossedOffHeader -> ListItemsCategoryTitle(
                    title = stringResource(R.string.crossed_off),
                    crossedOff = true,
                    isCard = isCard,
                )

                is ListRow.Item -> {
                    if (isCard) {
                        ListItemCard(
                            row = row,
                            settings = state.settings,
                            expandTagPills = expandTagPills,
                            onClick = { onItemClick(row.item) },
                            onEdit = { onItemEdit(row.item) },
                            onTagPillsClick = onTagPillsClick,
                        )
                    } else {
                        ListItemRow(
                            row = row,
                            settings = state.settings,
                            expandTagPills = expandTagPills,
                            onClick = { onItemClick(row.item) },
                            onEdit = { onItemEdit(row.item) },
                            onTagPillsClick = onTagPillsClick,
                        )
                    }
                }

                ListRow.DeleteCrossedOff -> DeleteCrossedOffItems(
                    onClick = onDeleteCrossedOff,
                    showDivider = !isCard,
                )
            }
        }
    }
}
//endregion


//region DeleteCrossedOffItems
@Composable
fun DeleteCrossedOffItems(onClick: () -> Unit, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = MaterialTheme.spaces.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                text = stringResource(R.string.delete_all_crossed_off_items),
            )
        }
        if (showDivider) HorizontalDivider()
    }
}
//endregion


//region ListItemsCategoryTitle
@Composable
fun ListItemsCategoryTitle(
    title: String,
    crossedOff: Boolean = false,
    isCard: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCard) Modifier
                else if (crossedOff) Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                else Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            )
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(
                    top = if (isCard) MaterialTheme.spaces.medium else MaterialTheme.spaces.small,
                    bottom = MaterialTheme.spaces.small
                ),
            color = when {
                !isCard -> MaterialTheme.colorScheme.onPrimaryContainer
                crossedOff -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.Bold,
            text = title,
        )
    }
}
//endregion


//region ListItem
@Composable
fun ListItemRow(
    row: ListRow.Item,
    settings: AppSettings,
    expandTagPills: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onTagPillsClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (row.item.crossedOff == true)
                        Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                    else Modifier
                )
                .clickable(onClick = onClick)
                .height(MaterialTheme.spaces.rowHeightMedium)
                .padding(
                    start = MaterialTheme.spaces.medium,
                    end = MaterialTheme.spaces.small
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListItemRowContent(
                row = row,
                settings = settings,
                expandTagPills = expandTagPills,
                isCard = false,
                onEdit = onEdit,
                onTagPillsClick = onTagPillsClick,
            )
        }
        HorizontalDivider()
    }
}
//endregion


//region ListItemCard
@Composable
fun ListItemCard(
    row: ListRow.Item,
    settings: AppSettings,
    expandTagPills: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onTagPillsClick: () -> Unit,
) {
    val crossedOff = row.item.crossedOff == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spaces.large,
                vertical = MaterialTheme.spaces.small,
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (crossedOff) MaterialTheme.colorScheme.surfaceContainer
            else MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = selectableCardBorder(false),
    ) {
        // click handling is inside the Card so the ripple is clipped to its shape
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListItemRowContent(
                row = row,
                settings = settings,
                expandTagPills = expandTagPills,
                isCard = true,
                onEdit = onEdit,
                onTagPillsClick = onTagPillsClick,
            )
        }
    }
}
//endregion


//region ListItemRowContent
// shared by the list row and the card; the only differences are the dividers
// and the empty photo placeholder, which only list mode needs
@Composable
fun RowScope.ListItemRowContent(
    row: ListRow.Item,
    settings: AppSettings,
    expandTagPills: Boolean,
    isCard: Boolean,
    onEdit: () -> Unit,
    onTagPillsClick: () -> Unit,
) {
    val item = row.item
    val targetSize = if (isCard) 48.dp else 44.dp

    if (settings.enablePhotos) {
        val photoShape = MaterialTheme.shapes.small
        if (item.photoUri.isNullOrEmpty()) {
            ListItemPhotoPlaceholder(photoShape, targetSize)
        } else {
            ListItemPhoto(
                item = item,
                showDivider = false,
                shape = photoShape,
                targetSize = targetSize
            )
        }
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = MaterialTheme.spaces.medium)
    ) {
        Text(
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                textDecoration = if (item.crossedOff == true) TextDecoration.LineThrough
                else TextDecoration.None
            ),
            text = item.quantity?.let { "${item.name} ($it)" } ?: item.name,
        )
        item.notes?.takeIf { it.isNotEmpty() }?.let { notes ->
            Text(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                text = notes,
            )
        }
    }

    if (settings.enableTags) {
        ListItemTags(
            tags = row.tags,
            expanded = expandTagPills,
            onClick = onTagPillsClick,
        )
    }

    IconButton(onClick = onEdit) {
        Icon(
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp),
            imageVector = MaterialIconsEdit,
            contentDescription = stringResource(R.string.view_item_details),
        )
    }
}
//endregion


//region ListItemPhoto
@Composable
fun ListItemPhoto(
    item: EasyListsListItem,
    showDivider: Boolean = false,
    targetSize: Dp = 48.dp,
    shape: Shape = RectangleShape,
) {
    val photoUri = item.photoUri

    if (photoUri.isNullOrEmpty()) {
        IconButton(onClick = { /* placeholder, nothing to do */ }) {
            Icon(
                imageVector = MaterialIconsBrokenImage,
                contentDescription = stringResource(R.string.view_item_details),
                tint = MaterialTheme.colorScheme.surface
            )
        }
    } else {
        FramedPhoto(
            photoUri = photoUri,
            scale = (item.photoScale?.toFloat() ?: 1f).takeIf { it > 0f } ?: 1f,
            normalizedOffsetX = item.photoOffsetX?.toFloat() ?: 0f,
            normalizedOffsetY = item.photoOffsetY?.toFloat() ?: 0f,
            zoomEnabled = false,
            targetSize = targetSize,
            shape = shape,
        )
    }
}
//endregion


//region ListItemPhotoPlaceholder
@Composable
fun ListItemPhotoPlaceholder(
    shape: Shape,
    targetSize: Dp = 48.dp,
) {
    Box(
        modifier = Modifier
            .size(targetSize)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = MaterialIconsInsertPhoto,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}
//endregion


//region Tags in the item row
@Composable
fun ListItemTags(
    tags: List<EasyListsTag>,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(end = MaterialTheme.spaces.medium),
        horizontalAlignment = Alignment.End
    ) {
        tags.take(2).forEach { tag ->
            if (expanded) TagPillSmall(tag, onClick)
            else TagDot(tag, onClick)
        }
    }
}

@Composable
fun TagDot(tag: EasyListsTag, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .padding(MaterialTheme.spaces.extraSmall)
            .clip(CircleShape)
            .background(tagBackground(tag, isSelected = true))
            .clickable(onClick = onClick)
    )
}

@Composable
fun TagPillSmall(
    tag: EasyListsTag,
    onClick: () -> Unit,
) {
    val backgroundColor = tagBackground(tag, isSelected = false)
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .padding(MaterialTheme.spaces.extraSmall)
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        Text(
            modifier = Modifier.padding(
                start = MaterialTheme.spaces.medium,
                end = MaterialTheme.spaces.medium
            ),
            color = backgroundColor.getContrastColor(),
            style = MaterialTheme.typography.labelSmall,
            text = tag.name
        )
    }
}

@Composable
private fun tagBackground(tag: EasyListsTag, isSelected: Boolean): Color =
    tag.color?.let { runCatching { Color(it.toColorInt()) }.getOrNull() }
        ?: if (isSelected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.tertiaryContainer
//endregion


//region Add / edit bottom sheet
class ItemEditorActions(
    val onNameChange: (String) -> Unit,
    val onQuantityChange: (String) -> Unit,
    val onNotesChange: (String) -> Unit,
    val onCategoryChange: (String) -> Unit,
    val onTagClick: (EasyListsTag) -> Unit,
    val onPhotoChosen: (String) -> Unit,
    val onPhotoTransformChanged: (Float, Float, Float) -> Unit,
    val onPhotoRemove: () -> Unit,
    val onSave: (onSaved: () -> Unit) -> Unit,
    val onDelete: () -> Unit,
    val onDismiss: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItemsBottomSheet(
    editor: ItemEditorState,
    validation: ItemEditorValidation,
    hasChanges: Boolean,
    categories: List<EasyListsCategory>,
    tags: List<EasyListsTag>,
    settings: AppSettings,
    actions: ItemEditorActions,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val photoActions = rememberPhotoActions(onPhotoChosen = actions.onPhotoChosen)

    // slide the sheet away, then clear the editor
    val hideAndDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) actions.onDismiss()
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = actions.onDismiss,
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
                            if (editor.mode == AddEditMode.Add) R.string.add_item
                            else R.string.edit_item
                        ),
                        icon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    enabled = validation.canSave && hasChanges,
                                    onClick = { actions.onSave(hideAndDismiss) },
                                ) {
                                    Icon(
                                        tint = if (hasChanges)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        imageVector = MaterialIconsCheck,
                                        contentDescription = stringResource(R.string.add_list_item),
                                    )
                                }
                                if (editor.mode != AddEditMode.Add) {
                                    IconButton(onClick = actions.onDelete) {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            imageVector = MaterialIconsDelete,
                                            contentDescription = stringResource(R.string.delete_item),
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
                        onValueChange = actions.onNameChange,
                        label = stringResource(R.string.name),
                        isError = validation.nameError != null,
                        errorMessage = if (validation.nameError == ItemNameError.Duplicate)
                            stringResource(R.string.item_name_in_use) else "",
                    )
                }

                item {
                    AppTextField(
                        value = editor.quantity,
                        onValueChange = actions.onQuantityChange,
                        label = stringResource(R.string.quantity),
                        keyboardType = KeyboardType.Number,
                    )
                }

                item {
                    ListItemBottomSheetCategory(
                        categoryName = editor.categoryName,
                        categories = categories,
                        onCategoryChange = actions.onCategoryChange,
                    )
                }

                item {
                    AppTextField(
                        value = editor.notes,
                        onValueChange = actions.onNotesChange,
                        label = stringResource(R.string.notes),
                        singleLine = false,
                    )
                }

                if (settings.enablePhotos) {
                    item {
                        ListItemBottomSheetPhotoTitle(
                            enableCamera = settings.enableCamera,
                            hasPhoto = editor.photo != null,
                            onTakePhoto = photoActions.takePhoto,
                            onPickPhoto = photoActions.pickPhoto,
                            onRemovePhoto = actions.onPhotoRemove,
                        )
                    }

                    editor.photo?.let { photo ->
                        item {
                            // keyed by uri so a newly chosen photo starts with fresh zoom state
                            key(photo.uri) {
                                FramedPhoto(
                                    photoUri = photo.uri,
                                    scale = photo.initial.scale,
                                    normalizedOffsetX = photo.initial.offsetX,
                                    normalizedOffsetY = photo.initial.offsetY,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f),
                                    zoomEnabled = true,
                                    onTransformChanged = actions.onPhotoTransformChanged,
                                    shape = MaterialTheme.shapes.small,
                                )
                            }
                        }
                    }
                }

                if (settings.enableTags && tags.isNotEmpty()) {
                    item {
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

                    item {
                        ListItemBottomSheetTags(
                            tags = tags,
                            selectedTagIds = editor.selectedTagIds,
                            onTagClick = actions.onTagClick,
                        )
                    }
                }
            }
        }
    }
}
//endregion


//region ListItemBottomSheetCategory
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItemBottomSheetCategory(
    categoryName: String,
    categories: List<EasyListsCategory>,
    onCategoryChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val isUncategorized = categoryName.isBlank()

    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.medium)
            .padding(bottom = MaterialTheme.spaces.large),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            label = { Text(text = stringResource(R.string.category)) },
            placeholder = { Text(text = stringResource(R.string.uncategorized)) },
            value = categoryName,
            onValueChange = onCategoryChange,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            // faux category: an empty name is saved as a null categoryId
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.uncategorized),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                onClick = {
                    onCategoryChange("")
                    expanded = false
                },
                trailingIcon = if (isUncategorized) {
                    { Icon(imageVector = MaterialIconsCheck, contentDescription = null) }
                } else null,
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )

            if (categories.isNotEmpty()) HorizontalDivider()

            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = {
                        onCategoryChange(category.name)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
//endregion


//region ListItemBottomSheetPhotoTitle
@Composable
fun ListItemBottomSheetPhotoTitle(
    enableCamera: Boolean,
    hasPhoto: Boolean,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
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

        if (enableCamera) {
            IconButton(modifier = Modifier.weight(0.13f), onClick = onTakePhoto) {
                Icon(
                    imageVector = MaterialIconsPhotoCamera,
                    contentDescription = stringResource(R.string.take_a_picture)
                )
            }
        }

        IconButton(modifier = Modifier.weight(0.13f), onClick = onPickPhoto) {
            Icon(
                imageVector = MaterialIconsInsertPhoto,
                contentDescription = stringResource(R.string.choose_photo)
            )
        }

        if (hasPhoto) {
            IconButton(modifier = Modifier.weight(0.13f), onClick = onRemovePhoto) {
                Icon(
                    imageVector = MaterialIconsDelete,
                    contentDescription = stringResource(R.string.remove_photo)
                )
            }
        }
    }
}
//endregion


//region ListItemBottomSheetTags
@Composable
fun ListItemBottomSheetTags(
    tags: List<EasyListsTag>,
    selectedTagIds: Set<String>,
    onTagClick: (EasyListsTag) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spaces.medium)
    ) {
        tags.forEach { tag ->
            TagPill(
                tag = tag,
                isSelected = tag.tagId?.let { it in selectedTagIds } == true,
                onClick = { onTagClick(tag) },
            )
        }
    }
}


@Composable
fun TagPill(
    tag: EasyListsTag,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = tagBackground(tag, isSelected)
    val shape = RoundedCornerShape(25.dp)

    Box(
        modifier = Modifier
            .padding(MaterialTheme.spaces.small)
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(
                start = MaterialTheme.spaces.medium,
                end = MaterialTheme.spaces.small
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                color = backgroundColor.getContrastColor(),
                modifier = Modifier.padding(end = MaterialTheme.spaces.small),
                style = MaterialTheme.typography.bodyMedium,
                text = tag.name
            )
            if (isSelected) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = MaterialIconsClose,
                    contentDescription = stringResource(R.string.remove_tag),
                    tint = backgroundColor.getContrastColor()
                )
            }
        }
    }
}
//endregion