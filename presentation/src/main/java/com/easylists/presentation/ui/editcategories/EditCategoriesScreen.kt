package com.easylists.presentation.ui.editcategories

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.EasyListsCategory
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.LocalCapitalization
import com.easylists.presentation.common.composables.AppTextField
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsCancel
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.MaterialIconsDelete
import com.easylists.presentation.models.CategoryPendingDelete
import com.easylists.presentation.models.CategorySheetState
import com.easylists.presentation.models.EditCategoriesState
import com.easylists.presentation.models.Screen
import com.easylists.presentation.models.usageFor
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.pop

//region EditCategoriesScreen :: stateful wrapper, the only place that knows about the ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoriesScreen(
    navController: NavController<Screen>,
    viewModel: EditCategoriesViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val s = settings ?: return

    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }

    // one-shot messages from the ViewModel
    val message = state.messageRes?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    // back leaves selection mode first, then the screen
    BackHandler(enabled = state.selectionMode, onBack = viewModel::exitSelectionMode)

    CompositionLocalProvider(LocalCapitalization provides s.capitalization) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.edit_tags)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.pop() }) {
                            Icon(
                                imageVector = MaterialIconsArrowBack,
                                contentDescription = stringResource(R.string.return_to_previous_screen),
                            )
                        }
                    },
                    actions = { EditCategoriesActions(state, viewModel) },
                )
            }
        ) { innerPadding ->
            EditCategoriesList(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding),
            )
        }

        state.categorySheet?.let { CategoryEditorSheet(sheet = it, state = state, viewModel = viewModel) }
        state.pendingDelete?.let { ConfirmDeleteDialog(pending = it, viewModel = viewModel) }
    }
}
//endregion





//region EditCategoriesActions
@Composable
private fun EditCategoriesActions(state: EditCategoriesState, viewModel: EditCategoriesViewModel) {
    if (state.selectionMode) {
        IconButton(
            enabled = state.selectedCategoryIds.isNotEmpty(),
            onClick = viewModel::requestDeleteSelected,
        ) {
            Icon(
                imageVector = MaterialIconsDelete,
                contentDescription = stringResource(R.string.delete_selected_tags),
            )
        }
        IconButton(onClick = viewModel::exitSelectionMode) {
            Icon(
                imageVector = MaterialIconsCancel,
                contentDescription = stringResource(R.string.cancel_deletion_of_selected_tags),
            )
        }
    } else {
        IconButton(onClick = viewModel::onAddCategoryClick) {
            Icon(
                imageVector = MaterialIconsAdd,
                contentDescription = stringResource(R.string.create_new_tag),
            )
        }
    }
}
//endregion


//region EditTagsList
@Composable
private fun EditCategoriesList(
    state: EditCategoriesState,
    viewModel: EditCategoriesViewModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { HorizontalDivider() }

        items(items = state.categoryList, key = { it.categoryId ?: it.name }) { category ->
            val categoryId = category.categoryId
            Column {
                CategoryRow(
                    category = category,
                    usageCount = categoryId?.let { state.categoryUsageCounts[it] } ?: 0,
                    selectionMode = state.selectionMode,
                    selected = categoryId != null && categoryId in state.selectedCategoryIds,
                    onClick = { viewModel.onCategoryClick(category) },
                    onLongClick = { viewModel.onCategoryLongClick(category) },
                    onToggleSelected = { categoryId?.let(viewModel::toggleSelection) },
                )
                HorizontalDivider()
            }
        }
    }
}
//endregion


//region TagRow
@Composable
private fun CategoryRow(
    category: EasyListsCategory,
    usageCount: Int,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.spaces.rowHeightMedium)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = MaterialTheme.spaces.medium)
            .padding(vertical = MaterialTheme.spaces.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MaterialTheme.spaces.medium),
        ) {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                text = category.name,
            )
            Text(
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                text = if (usageCount == 0) {
                    stringResource(R.string.category_not_used)
                } else {
                    pluralStringResource(R.plurals.category_used_by_list_items, usageCount, usageCount)
                },
            )
        }

        if (selectionMode) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggleSelected() },
            )
        }
    }
}
//endregion


//region ConfirmDeleteDialog
@Composable
private fun ConfirmDeleteDialog(pending: CategoryPendingDelete, viewModel: EditCategoriesViewModel) {
    ConfirmationDialog(
        onDismissRequest = viewModel::onDeleteDismissed,
        onConfirmation = viewModel::onDeleteConfirmed,
        dialogTitle = stringResource(R.string.confirm_deletion),
        dialogText = stringResource(
            if (pending is CategoryPendingDelete.Single) R.string.delete_category_warning
            else R.string.delete_categories_warning
        ),
    )
}
//endregion


//region SheetDragHandle
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetDragHandle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomSheetDefaults.DragHandle()
    }
}
//endregion


//region TagEditorSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorSheet(
    sheet: CategorySheetState,
    state: EditCategoriesState,
    viewModel: EditCategoriesViewModel,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val usage = remember(sheet.categoryId, state.listItemList, state.listList) {
        state.usageFor(sheet.categoryId)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = viewModel::onCategorySheetDismiss,
        dragHandle = { SheetDragHandle() },
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
                            if (sheet.mode == AddEditMode.Add) R.string.add_category else R.string.edit_category
                        ),
                        icon = {
                            IconButton(
                                enabled = state.canSaveCategory,
                                onClick = viewModel::saveCategory,
                            ) {
                                Icon(
                                    imageVector = MaterialIconsCheck,
                                    contentDescription = stringResource(R.string.save_category),
                                )
                            }
                            if (sheet.mode == AddEditMode.Edit) {
                                IconButton(onClick = { viewModel.requestDeleteCategory(sheet.categoryId) }) {
                                    Icon(
                                        imageVector = MaterialIconsDelete,
                                        contentDescription = stringResource(R.string.delete_category),
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                    )
                }

                item {
                    val duplicate = state.isTagNameDuplicate
                    AppTextField(
                        value = sheet.name,
                        onValueChange = viewModel::onCategoryNameChange,
                        label = stringResource(R.string.name),
                        isError = duplicate,
                        errorMessage = if (duplicate) stringResource(R.string.category_name_in_use) else "",
                    )
                }

                if (usage.isEmpty()) {
                    if (sheet.mode == AddEditMode.Edit) {
                        item {
                            Text(
                                modifier = Modifier
                                    .padding(top = MaterialTheme.spaces.medium)
                                    .padding(horizontal = MaterialTheme.spaces.medium),
                                style = MaterialTheme.typography.bodyLarge,
                                text = stringResource(R.string.category_not_used_message),
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            modifier = Modifier
                                .padding(top = MaterialTheme.spaces.medium)
                                .padding(horizontal = MaterialTheme.spaces.medium),
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(R.string.category_used_by_message),
                        )
                    }
                    items(usage) { entry ->
                        Column(modifier = Modifier.padding(top = MaterialTheme.spaces.medium)) {
                            Text(
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                                style = MaterialTheme.typography.bodyLarge,
                                text = entry.list.name,
                            )
                            entry.items.forEach { listItem ->
                                Text(
                                    text = listItem.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = MaterialTheme.spaces.extraLarge)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
//endregion
