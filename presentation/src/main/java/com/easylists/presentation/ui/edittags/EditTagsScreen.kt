package com.easylists.presentation.ui.edittags

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
import com.easylists.domain.models.EasyListsTag
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.EditTagsAction
import com.easylists.presentation.common.composables.ConfirmationDialog
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.Arrow_back
import com.easylists.presentation.icons.Cancel
import com.easylists.presentation.icons.Check
import com.easylists.presentation.icons.Delete
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import com.toxicbakery.logging.Arbor
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagsScreen(
    navController: NavController<Screen>,
    viewModel: EditTagsViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    when {
        viewModel.state.nextStep == "remove_tags" -> {
            viewModel.removeTags()
        }
    }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { EditTagsScreenTitle() },
                navigationIcon = { EditTagsScreenTopAppBarNavigationIcon(navController) },
                actions = { EditTagsScreenActionIcons(viewModel) },
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

            ConfirmRemove(viewModel)

            EditTagsScreenCategoryBottomSheet(viewModel)

            EditTagsScreenContent(viewModel)

        }
    }

}


//region EditTagsScreenTitle
@Composable
fun EditTagsScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.edit_tags))
    }
}
//endregion


//region EditTagsScreenTopAppBarNavigationIcon
@Composable
fun EditTagsScreenTopAppBarNavigationIcon(navController: NavController<Screen>) {
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


//region EditTagsScreenActionIcons
@Composable
fun EditTagsScreenActionIcons(viewModel: EditTagsViewModel) {
    when (viewModel.state.actionButtonState) {
        EditTagsAction.Remove -> {
            val title = stringResource(R.string.confirm_removal)
            val message = stringResource(R.string.remove_tags_warning)
            IconButton(
                enabled = viewModel.state.tagList.any { it.selectedForRemoval },
                onClick = {
                    viewModel.configureRemoveTag(
                        title = title,
                        message = message,
                        onConfirmation = {
                            viewModel.deselectCheckboxes()
                            viewModel.removeTagFromListItems()
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
                    contentDescription = stringResource(R.string.remove_selected_tags)
                )
            }
            IconButton(onClick = {
                viewModel.showContextIcons()
            }) {
                Icon(
                    modifier = Modifier,
                    imageVector = Cancel,
                    contentDescription = stringResource(R.string.cancel_removal_of_selected_tags)
                )
            }
        }

        else -> {
            IconButton(onClick = {
                viewModel.showTagBottomSheet()
            }) {
                Icon(
                    modifier = Modifier,
                    imageVector = Add,
                    contentDescription = stringResource(R.string.create_new_tag)
                )
            }
        }
    }
}
//endregion


//region EditTagsScreenCategoryBottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagsScreenCategoryBottomSheet(viewModel: EditTagsViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet = remember { mutableStateOf(false) }

    when (viewModel.state.showTagBottomSheet) {
        true -> showBottomSheet.value = true
        false -> showBottomSheet.value = false
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet.value = false
                viewModel.onTagBottomSheetDismiss()
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
                                if (viewModel.state.addEditMode == AddEditMode.Add) R.string.add_tag
                                else R.string.edit_tag
                            ),
                            icon = {
                                IconButton(
                                    enabled = viewModel.tagIconButtonEnabled(),
                                    onClick = {
                                        if (viewModel.state.addEditMode == AddEditMode.Add) viewModel.addTag()
                                        else viewModel.updateTag()
                                    },
                                ) {
                                    Icon(
                                        imageVector = Check,
                                        contentDescription = stringResource(R.string.add_tag),
                                    )
                                }
                                if (viewModel.state.addEditMode == AddEditMode.Edit) {
                                    val title = stringResource(R.string.confirm_removal)
                                    val message = stringResource(R.string.remove_tag_warning)
                                    IconButton(
                                        enabled = true,
                                        onClick = {
                                            viewModel.configureRemoveTag(
                                                title = title,
                                                message = message,
                                                onConfirmation = {
                                                    viewModel.onTagSelectedForRemovalChanged(viewModel.state.selectedItem?.uid)
                                                    viewModel.removeTags()
                                                    viewModel.dismissConfirmationDialog()
                                                    viewModel.showTagBottomSheet()
                                                },
                                                onDismissRequest = {
                                                    viewModel.dismissConfirmationDialog()
                                                }
                                            )
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Delete,
                                            contentDescription = stringResource(R.string.add_tag),
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
                        )
                    }

                    item {
                        EditTagsScreenBottomSheetName(viewModel)
                    }

                    item {
                        EditTagsScreenBottomSheetListsAndItems(viewModel)
                    }

                }
            }
        }
    }
}
//endregion


//region EditTagsScreenBottomSheetListsAndItems
@Composable
fun EditTagsScreenBottomSheetListsAndItems(viewModel: EditTagsViewModel) {
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
                    text = stringResource(R.string.tag_not_used_message),
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium),
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(R.string.tag_used_by_message),
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


//region EditTagsScreenBottomSheetName
@Composable
fun EditTagsScreenBottomSheetName(viewModel: EditTagsViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.medium)
                .padding(top = MaterialTheme.spaces.medium),
            value = viewModel.tagName(),
            onValueChange = { viewModel.onTagNameChange(it) },
            label = { Text(text = stringResource(R.string.name)) },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                capitalization = viewModel.state.capitalization.keyboardCapitalization,
                keyboardType = KeyboardType.Text,
                autoCorrectEnabled = true,
            ),
            isError = viewModel.state.tagNameInvalid,
            supportingText = {
                when {
                    viewModel.state.tagNameInvalidMessage.isNotEmpty() == true ->
                        Text(text = viewModel.state.tagNameInvalidMessage)

                    else -> null
                }
            }
        )
    }
}
//endregion


//region EditTagsScreenContent
@Composable
fun EditTagsScreenContent(viewModel: EditTagsViewModel) {
    val lazyColumnState = rememberLazyListState()
    LazyColumn(
        state = lazyColumnState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.none),
    ) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }

        viewModel.state.tagList.forEach { item ->
            item {
                EditTagsScreenTag(item, viewModel)
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
            }
        }
    }
}
//endregion


//region EditTagsScreenTag
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTagsScreenTag(
    item: EasyListsTag,
    viewModel: EditTagsViewModel
) {
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spaces.none)
            .combinedClickable(
                onClick = { viewModel.onTagClick(item) },
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
                val tagListItemCount = viewModel.tagListItemCount(item)
                Text(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    text = when (tagListItemCount) {
                        0 -> "Not used"
                        1 -> "Used by $tagListItemCount list item"
                        else -> "Used by $tagListItemCount list items"
                    }
                )
            }
            TagCheckbox(item, viewModel)
        }
    }
}
//endregion


//region TagCheckbox
@Composable
fun TagCheckbox(
    item: EasyListsTag,
    viewModel: EditTagsViewModel
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
                    viewModel.onTagSelectedForRemovalChanged(item.uid)
                },
            )
        }
    }
}
//endregion


//region ConfirmRemoveTags
@Composable
fun ConfirmRemove(viewModel: EditTagsViewModel) {
    when {
        viewModel.state.showConfirmationDialog == true -> {
            ConfirmationDialog(
                onDismissRequest = viewModel.state.confirmationOnDismissRequest,
                onConfirmation = viewModel.state.confirmationOnConfirmation,
                dialogTitle = viewModel.state.confirmationTitle,
                dialogText = viewModel.state.confirmationMessage,
            )

//            ConfirmationDialog(
//                onDismissRequest = {
//                    viewModel.setShowConfirmationDialogState(false)
//                },
//                onConfirmation = {
//                    viewModel.deselectCheckboxes()
//                    viewModel.removeTagFromListItems()
//                    viewModel.setShowConfirmationDialogState(false)
//                },
//                dialogTitle = stringResource(R.string.confirm_removal),
//                dialogText = stringResource(R.string.remove_tags_warning),
//            )
        }
    }
}
//endregion
