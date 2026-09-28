package com.easylists.presentation.ui.editcategories

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.easylists.domain.models.EasyListsCategory
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.EditCategoriesAction
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.MaterialIconsAdd
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsCancel
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoriesScreen(
    navController: NavController<Screen>,
    viewModel: EditCategoriesViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    when {
        viewModel.state.nextStep == "remove_categories" -> {
            viewModel.removeCategories()
        }
    }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { EditCategoriesScreenTitle() },
                navigationIcon = { EditCategoriesScreenTopAppBarNavigationIcon(navController) },
                actions = { EditCategoriesScreenActionIcons(viewModel) },
            )
        }
    ) { innerPadding ->

        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = viewModel.state.isPullToRefreshing,
            onRefresh = viewModel.onPullToRefresh(),
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

            ConfirmRemove(viewModel)

            EditCategoriesScreenCategoryBottomSheet(viewModel)

            EditCategoriesScreenContent(viewModel)

        }
    }

}


//region EditCategoriesScreenTitle
@Composable
fun EditCategoriesScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.edit_categories))
    }
}
//endregion


//region EditCategoriesScreenTopAppBarNavigationIcon
@Composable
fun EditCategoriesScreenTopAppBarNavigationIcon(navController: NavController<Screen>) {
    IconButton(
        onClick = { navController.pop() }
    ) {
        Icon(
            painter = rememberVectorPainter(MaterialIconsArrowBack),
            contentDescription = stringResource(R.string.return_to_previous_screen),
            modifier = Modifier.padding(start = MaterialTheme.spaces.mediumLarge),
        )
    }
}
//endregion


//region EditCategoriesScreenActionIcons
@Composable
fun EditCategoriesScreenActionIcons(viewModel: EditCategoriesViewModel) {
    when (viewModel.state.actionButtonState) {
        EditCategoriesAction.Remove -> {
            val title = stringResource(R.string.confirm_removal)
            val message = stringResource(R.string.remove_categories_warning)
            IconButton(
                enabled = viewModel.state.categoryList.any { it.selectedForRemoval },
                onClick = {
                    viewModel.configureRemoveCategory(
                        title = title,
                        message = message,
                        onConfirmation = {
                            viewModel.deselectCheckboxes()
                            viewModel.removeCategoryFromListItems()
                            viewModel.setShowConfirmationDialogState(false)
                        },
                        onDismissRequest = {
                            viewModel.dismissConfirmationDialog()
                        }
                    )
                }
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = Delete,
                    contentDescription = stringResource(R.string.remove_selected_categories)
                )
            }
            IconButton(onClick = {
                viewModel.showContextIcons()
            }) {
                Icon(
                    modifier = Modifier,
                    imageVector = MaterialIconsCancel,
                    contentDescription = stringResource(R.string.cancel_removal_of_selected_categories)
                )
            }
        }

        else -> {
            IconButton(onClick = {
                viewModel.showCategoryBottomSheet()
            }) {
                Icon(
                    modifier = Modifier,
                    imageVector = MaterialIconsAdd,
                    contentDescription = stringResource(R.string.create_new_list)
                )
            }
        }
    }
}
//endregion


//region EditCategoriesScreenCategoryBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoriesScreenCategoryBottomSheet(viewModel: EditCategoriesViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet = remember { mutableStateOf(false) }

    when (viewModel.state.showCategoryBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet.value = false
                viewModel.onCategoryBottomSheetDismiss()
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
                                if (viewModel.state.addEditMode == AddEditMode.Add) R.string.add_category
                                else R.string.edit_category
                            ),
                            icon = {
                                IconButton(
                                    enabled = viewModel.categoryIconButtonEnabled(),
                                    onClick = {
                                        if (viewModel.state.addEditMode == AddEditMode.Add) viewModel.addCategory()
                                        else viewModel.updateCategory()
                                    },
                                ) {
                                    Icon(
                                        imageVector = MaterialIconsCheck,
                                        contentDescription = stringResource(R.string.add_category),
                                    )
                                }
                                if (viewModel.state.addEditMode == AddEditMode.Edit) {
                                    val title = stringResource(R.string.confirm_removal)
                                    val message = stringResource(R.string.remove_category_warning)
                                    IconButton(
                                        enabled = true,
                                        onClick = {
                                            viewModel.configureRemoveCategory(
                                                title = title,
                                                message = message,
                                                onConfirmation = {
                                                    viewModel.onCategorySelectedForRemovalChanged(viewModel.state.selectedItem?.uid)
                                                    viewModel.removeCategories()
                                                    viewModel.dismissConfirmationDialog()
                                                    viewModel.showCategoryBottomSheet()
                                                },
                                                onDismissRequest = {
                                                    viewModel.dismissConfirmationDialog()
                                                }
                                            )
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Delete,
                                            contentDescription = stringResource(R.string.add_category),
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                        )
                    }

                    item {
                        EditCategoriesScreenBottomSheetName(viewModel)
                    }

                    item {
                        EditCategoriesScreenBottomSheetListsAndItems(viewModel)
                    }

                }
            }
        }
    }
}
//endregion


//region EditCategoriesScreenBottomSheetName
@Composable
fun EditCategoriesScreenBottomSheetName(viewModel: EditCategoriesViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.categoryName(),
            onValueChange = { viewModel.onCategoryNameChange(it) },
            label = { Text(text = stringResource(R.string.name)) },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                capitalization = viewModel.state.capitalization.keyboardCapitalization,
                keyboardType = KeyboardType.Text,
                autoCorrectEnabled = true,
            ),
            isError = viewModel.state.categoryNameInvalid,
            supportingText = {
                when {
                    viewModel.state.categoryNameInvalidMessage.isNotEmpty() == true ->
                        Text(text = viewModel.state.categoryNameInvalidMessage)

                    else -> null
                }
            }
        )
    }
}
//endregion


//region EditCategoriesScreenBottomSheetListsAndItems
@Composable
fun EditCategoriesScreenBottomSheetListsAndItems(viewModel: EditCategoriesViewModel) {
    var lists = viewModel.lists()
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = MaterialTheme.spaces.large)
        .padding(top = MaterialTheme.spaces.medium)
    ) {
        if (lists.isEmpty()) {
            if (viewModel.state.addEditMode == AddEditMode.Edit) {
                Text(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(R.string.category_not_used_message),
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(R.string.category_used_by_message),
            )
        }
    }
    lists.forEach { list ->
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spaces.large)
            .padding(top = MaterialTheme.spaces.medium)
        ) {
            Text(
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                style = MaterialTheme.typography.bodyLarge,
                text = list.name,
            )

            val listItems = viewModel.listItems()
            listItems.forEach { item ->
                if (item.listUid == list.uid) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spaces.extraLarge)
                    )
                }
            }
        }
    }
}
//endregion


//region EditCategoriesScreenContent
@Composable
fun EditCategoriesScreenContent(viewModel: EditCategoriesViewModel) {
    val lazyColumnState = rememberLazyListState()
    LazyColumn(
        state = lazyColumnState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.none),
    ) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }

        viewModel.state.categoryList.forEach { item ->
            item {
                EditCategoriesScreenCategory(item, viewModel)
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
            }
        }
    }
}
//endregion


//region EditCategoriesScreenCategory
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditCategoriesScreenCategory(
    item: EasyListsCategory,
    viewModel: EditCategoriesViewModel
) {
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.none)
            .combinedClickable(
                onClick = { viewModel.onCategoryClick(item) },
                onLongClick = { viewModel.showContextIcons(item) }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.spaces.rowHeightMedium)
                .padding(start = MaterialTheme.spaces.medium)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.spaces.medium),
            ) {
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        textDecoration = TextDecoration.None,
                    ),
                    text = item.name,
                )
                val listItemCount = viewModel.categoryListItemCount(item)
                Text(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    text = when (listItemCount) {
                        0 -> "Not used"
                        1 -> "Used by $listItemCount list item"
                        else -> "Used by $listItemCount list items"
                    }
                )
            }
            CategoryCheckbox(item, viewModel)
        }
    }
}
//endregion


//region CategoryCheckbox
@Composable
fun CategoryCheckbox(
    item: EasyListsCategory,
    viewModel: EditCategoriesViewModel
) {
    val (checkedState, onStateChange) = remember { mutableStateOf(false) }

    // uncheck item when context items are not shown
    when {
        viewModel.state.showContextItems == false || viewModel.state.deselectCheckboxes == true -> {
            onStateChange(false)
        }
    }

    when {
        viewModel.state.showContextItems == true -> {
            Checkbox(
                modifier = Modifier.padding(MaterialTheme.spaces.none),
                checked = checkedState,
                onCheckedChange = {
                    onStateChange(!checkedState)
                    viewModel.onCategorySelectedForRemovalChanged(item.uid)
                },
            )
        }
    }
}
//endregion


//region ConfirmRemove
@Composable
fun ConfirmRemove(viewModel: EditCategoriesViewModel) {
    when {
        viewModel.state.showConfirmationDialog == true -> {
            ConfirmationDialog(
                onDismissRequest = viewModel.state.confirmationOnDismissRequest,
                onConfirmation = viewModel.state.confirmationOnConfirmation,
                dialogTitle = viewModel.state.confirmationTitle,
                dialogText = viewModel.state.confirmationMessage,
            )
        }
    }
}
//endregion
