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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.EditCategoriesAction
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Arrow_back
import com.easylists.presentation.icons.Cancel
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.icons.Info
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.listdetails.ListDetailsScreenCategoryTitle
import com.easylists.presentation.ui.listdetails.ListDetailsScreenDeleteCrossedOffItems
import com.easylists.presentation.ui.listdetails.ListDetailsScreenListItem
import com.easylists.presentation.ui.listdetails.ListDetailsScreenListItemBottomSheetCategory
import com.easylists.presentation.ui.listdetails.ListDetailsScreenListItemBottomSheetName
import com.easylists.presentation.ui.listdetails.ListDetailsScreenListItemBottomSheetNotes
import com.easylists.presentation.ui.listdetails.ListDetailsScreenListItemBottomSheetQuantity
import com.easylists.presentation.ui.listdetails.ListDetailsViewModel
import com.easylists.presentation.ui.masterlists.MasterListsViewModel
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

            ConfirmRemoveCategories(viewModel)

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
            painter = rememberVectorPainter(Arrow_back),
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
            IconButton(
                enabled = viewModel.state.categoryList.any { it.selectedForRemoval },
                onClick = {
                    viewModel.setShowConfirmationDialogState(true)
                }
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = Check,
                    contentDescription = stringResource(R.string.remove_selected_categories)
                )
            }
            IconButton(onClick = {
                viewModel.showContextIcons()
            }) {
                Icon(
                    modifier = Modifier,
                    imageVector = Cancel,
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
                    imageVector = Add,
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
                                if (viewModel.state.addEditMode == AddEditMode.Add) R.string.add_item
                                else R.string.edit_item
                            ),
                            icon = {
                                IconButton(
                                    enabled = viewModel.categoryIconButtonEnabled(),
                                    onClick = { viewModel.addCategory() },
                                ) {
                                    Icon(
                                        imageVector = Check,
                                        contentDescription = stringResource(R.string.add_category),
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                        )
                    }

                    item {
                        EditCategoriesScreenBottomSheetName(viewModel)
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                .height(52.dp)
                .padding(start = MaterialTheme.spaces.medium)
                .padding(vertical = MaterialTheme.spaces.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryCheckbox(item, viewModel)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal =
                        if (viewModel.state.showContextItems) MaterialTheme.spaces.none
                        else MaterialTheme.spaces.medium),
            ) {
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = item.name,
                )
            }
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


//region ConfirmRemoveCategories
@Composable
fun ConfirmRemoveCategories(viewModel: EditCategoriesViewModel) {
    when {
        viewModel.state.showConfirmationDialog == true -> {
            ConfirmationDialog(
                onDismissRequest = {
                    viewModel.setShowConfirmationDialogState(false)
                },
                onConfirmation = {
                    viewModel.deselectCheckboxes()
                    viewModel.removeCategoryFromListItems()
                    viewModel.setShowConfirmationDialogState(false)
                },
                dialogTitle = stringResource(R.string.confirm_removal),
                dialogText = stringResource(R.string.remove_categories_warning),
            )
        }
    }
}
//endregion