package com.easylists.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.easylists.domain.common.Themes
import com.easylists.presentation.R
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.common.composables.ListSettingGroup
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.common.composables.ToggleSettingItem
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsInfo
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController<Screen>,
    sharedViewModel: SharedViewModel,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { SettingsScreenTitle() },
                navigationIcon = { SettingsScreenTopAppBarNavigationIcon(navController) },
                actions = { SettingsScreenActionIcons(navController) },
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

            SettingsScreenContent(navController, viewModel)

        }
    }

}


//region SettingsScreenTitle
@Composable
fun SettingsScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.settings))
    }
}
//endregion


//region SettingsScreenActionIcons
@Composable
fun SettingsScreenActionIcons(navController: NavController<Screen>) {
    IconButton(onClick = { navController.navigate(Screen.About) }) {
        Icon(
            modifier = Modifier,
            imageVector = MaterialIconsInfo,
            contentDescription = stringResource(R.string.about),
        )
    }
}
//endregion


//region SettingsScreenTopAppBarNavigationIcon
@Composable
fun SettingsScreenTopAppBarNavigationIcon(navController: NavController<Screen>) {
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


//region SettingsScreenContent
@Composable
fun SettingsScreenContent(
    navController: NavController<Screen>,
    viewModel: SettingsViewModel
) {

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            SectionTitle(
                title = stringResource(id = R.string.list_items),
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spaces.large)
                    .padding(top = MaterialTheme.spaces.medium)
            )
        }
        item {
            ListSettingGroup(
                stringResource(R.string.group_crossed_off_items),
                GroupCrossedOffItems.entries.toList(),
                GroupCrossedOffItems.entries.indexOf(viewModel.state.groupCrossedOffItems),
                viewModel
            )
        }
        item {
            ListSettingGroup(
                stringResource(R.string.sort_crossed_off_items),
                SortCrossedOffItems.entries.toList(),
                SortCrossedOffItems.entries.indexOf(viewModel.state.sortCrossedOffItems),
                viewModel
            )
        }
        item {
            ToggleSettingItem(
                textLine1 = stringResource(id = R.string.enable_camera),
                textLine2 = stringResource(id = R.string.enable_camera_description),
                enabled = viewModel.state.enableCamera,
                onCheckedChange = { viewModel.onEnableCameraChanged() }
            )
        }
        item {
            ToggleSettingItem(
                textLine1 = stringResource(id = R.string.enable_photos),
                textLine2 = stringResource(id = R.string.enable_photos_description),
                enabled = viewModel.state.enablePhotos,
                onCheckedChange = { viewModel.onEnablePhotosChanged() }
            )
        }
        item {
            ToggleSettingItem(
                textLine1 = stringResource(id = R.string.enable_tags),
                textLine2 = stringResource(id = R.string.enable_tags_description),
                enabled = viewModel.state.enableTags,
                onCheckedChange = { viewModel.onEnableTagsChanged() }
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.medium))
        }
        item {
            SectionTitle(
                title = stringResource(id = R.string.display),
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large)
            )
        }
        item {
            ListSettingGroup(
                stringResource(R.string.theme),
                Themes.entries.toList(),
                Themes.entries.indexOf(viewModel.state.theme),
                viewModel
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spaces.medium))
        }
        item {
            SectionTitle(
                title = stringResource(id = R.string.adding_and_editing),
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large)
            )
        }
        item {
            ListSettingGroup(
                stringResource(R.string.capitalization),
                Capitalization.entries.toList(),
                Capitalization.entries.indexOf(viewModel.state.capitalization),
                viewModel
            )
        }
    }

}
//endregion
