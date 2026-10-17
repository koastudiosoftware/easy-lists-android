package com.easylists.presentation.ui.about

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.easylists.presentation.R
import com.easylists.presentation.common.PRIVACY_POLICY_URL
import com.easylists.presentation.common.SUPPORT_EMAIL
import com.easylists.presentation.common.USER_GUIDE_URL
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.common.composables.CardGroup
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsKeyboardArrowRight
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController<Screen>,
    onLinkClick: (String) -> Unit,
    onEmailClick: (String, String) -> Unit,
    onPlayStoreClick: () -> Unit,
    onSubscribeClick: () -> Unit = {},
    viewModel: AboutViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { AboutScreenTitle() },
                navigationIcon = { AboutScreenTopAppBarNavigationIcon(navController) },
            )
        }
    ) { innerPadding ->

        AboutScreenContent(
            innerPadding,
            onLinkClick,
            onEmailClick,
            onPlayStoreClick,
            onSubscribeClick,
            navController,
            viewModel
        )

    }
}


//region AboutScreenTitle
@Composable
fun AboutScreenTitle() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = stringResource(R.string.about))
    }
}
//endregion


//region AboutScreenTopAppBarNavigationIcon
@Composable
fun AboutScreenTopAppBarNavigationIcon(navController: NavController<Screen>) {
    IconButton(
        onClick = { navController.pop() }
    ) {
        Icon(
            imageVector = MaterialIconsArrowBack,
            contentDescription = stringResource(R.string.return_to_previous_screen),
        )
    }
}
//endregion


//region AboutScreenContent
@Composable
fun AboutScreenContent(
    innerPadding: PaddingValues,
    onLinkClick: (String) -> Unit,
    onEmailClick: (String, String) -> Unit,
    onPlayStoreClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    navController: NavController<Screen>,
    viewModel: AboutViewModel,
) {
    // TODO placeholder: replace with the real time of the last server check once there is one.
    // Pass null to show "not checked yet".
    val lastServerCheckMillis: Long? = remember { System.currentTimeMillis() - 5 * 60 * 1000L }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        item {
            Spacer(modifier = Modifier.size(MaterialTheme.spaces.large))
        }

        item {
            AppLogo(modifier = Modifier.fillMaxWidth())
        }

        item {
            Spacer(modifier = Modifier.size(MaterialTheme.spaces.large))
        }

        item {
            Version(modifier = Modifier.fillMaxWidth(), viewModel)
        }

        item {
            Copyright(modifier = Modifier.fillMaxWidth())
        }

        item {
            LastServerCheck(
                lastCheckMillis = lastServerCheckMillis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            Spacer(modifier = Modifier.size(MaterialTheme.spaces.large))
        }

        //region Help
        item {
            SectionTitle(
                modifier = Modifier.padding(MaterialTheme.spaces.large),
                title = stringResource(R.string.about_section_help),
            )
        }

        item {
            CardGroup(
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
            ) {
                AboutLinkRow(
                    title = stringResource(R.string.about_user_guide),
                    summary = stringResource(R.string.about_user_guide_summary),
                    onClick = { onLinkClick(USER_GUIDE_URL) },
                )
                HorizontalDivider()
                AboutLinkRow(
                    title = stringResource(R.string.about_contact_support),
                    summary = stringResource(R.string.about_contact_support_summary),
                    onClick = { onEmailClick(SUPPORT_EMAIL, "[Easy Lists App Support]") },
                )
                HorizontalDivider()
                AboutLinkRow(
                    title = stringResource(R.string.about_privacy_policy),
                    onClick = { onLinkClick(PRIVACY_POLICY_URL) },
                )
            }
        }
        //endregion

        //region Support Easy Lists
        item {
            SectionTitle(
                modifier = Modifier.padding(MaterialTheme.spaces.large),
                title = stringResource(R.string.about_section_support_us),
            )
        }

        item {
            CardGroup(
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
            ) {
                AboutLinkRow(
                    title = stringResource(R.string.about_rate_us),
                    summary = stringResource(R.string.about_rate_us_summary),
                    onClick = onPlayStoreClick,
                )
                HorizontalDivider()
                AboutLinkRow(
                    title = stringResource(R.string.about_subscribe),
                    summary = stringResource(R.string.about_subscribe_summary),
                    onClick = onSubscribeClick,
                )
            }
        }
        //endregion

        item {
            Spacer(modifier = Modifier.size(MaterialTheme.spaces.extraLarge))
        }
    }
}
//endregion


//region AppLogo
@Composable
private fun AppLogo(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.requiredHeight(180.dp),
            contentDescription = stringResource(R.string.app_name) + " logo",
            contentScale = ContentScale.Fit,
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
        )
        Text(
            modifier = Modifier.padding(top = MaterialTheme.spaces.small),
            text = stringResource(id = R.string.app_name),
            fontSize = MaterialTheme.typography.bodyLarge.lineHeight,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
    }
}
//endregion


//region Copyright
@Composable
private fun Copyright(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.spaces.small),
            text = stringResource(id = R.string.copyright),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
//endregion


//region Version
@Composable
private fun Version(modifier: Modifier = Modifier, viewModel: AboutViewModel) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.spaces.small),
            text = stringResource(
                id = R.string.app_version, viewModel.appVersion.name,
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
//endregion


//region LastServerCheck
// "Last server check: 5 minutes ago", or a "not checked yet" line when there has been no check.
// The relative time comes from the system, so it follows the user's language.
@Composable
private fun LastServerCheck(lastCheckMillis: Long?, modifier: Modifier = Modifier) {
    val text = if (lastCheckMillis == null) {
        stringResource(R.string.about_last_server_check_never)
    } else {
        val ago = DateUtils.getRelativeTimeSpanString(
            lastCheckMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
        stringResource(R.string.about_last_server_check, ago)
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.spaces.small),
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
//endregion


//region AboutLinkRow
// One tappable row inside a SettingsCardGroup: a title, an optional summary and a chevron.
// leadingIcon is optional so icons can be added later without changing the call sites.
@Composable
private fun AboutLinkRow(
    title: String,
    onClick: () -> Unit,
    summary: String? = null,
    leadingIcon: ImageVector? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (summary != null) {
            { Text(summary) }
        } else {
            null
        },
        leadingContent = if (leadingIcon != null) {
            { Icon(imageVector = leadingIcon, contentDescription = null) }
        } else {
            null
        },
        trailingContent = {
            Icon(
                imageVector = MaterialIconsKeyboardArrowRight,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
//endregion