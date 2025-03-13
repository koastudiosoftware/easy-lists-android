package com.easylists.presentation.ui.listoflists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easylists.presentation.R
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.icons.Add
import com.easylists.presentation.icons.More_vert
import com.easylists.presentation.icons.Settings
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import com.toxicbakery.logging.Arbor
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel

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
                actions = { ListOfListsScreenActionIcons(viewModel) },
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

            ListOfListsScreenContent(viewModel)

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
fun ListOfListsScreenActionIcons(viewModel: ListOfListsViewModel) {
    IconButton(onClick = {
        viewModel.onActionButtonClick(ADD)
    }) {
        Icon(
            modifier = Modifier,
            imageVector = Add,
            contentDescription = stringResource(R.string.create_new_list)
        )
    }
    ListOfListsScreenOverflowMenu(viewModel)
}
//endregion


//region ListOfListsScreenContent
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListOfListsScreenContent(viewModel: ListOfListsViewModel) {
    val lazyColumnState = rememberLazyListState()
    LazyColumn(
        state = lazyColumnState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.medium),
    ) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }
        itemsIndexed(viewModel.lists) { _, item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .heightIn(0.dp, 64.dp)
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(
                    top = MaterialTheme.spaces.medium,
                    bottom = MaterialTheme.spaces.large
                )
                .combinedClickable(
                    onClick = {
                        Arbor.i("Clicked on $item")
//                        viewModel.onItemClick(item)
                    },
                    onLongClick = {
                        Arbor.i("Long clicked on $item")
//                        viewModel.onItemLongClick(item)
                    }
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = item
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.none))
        }
    }
}
//endregion


//region ListOfListsScreenOverflowMenu
@Composable
fun ListOfListsScreenOverflowMenu(viewModel: ListOfListsViewModel) {
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
