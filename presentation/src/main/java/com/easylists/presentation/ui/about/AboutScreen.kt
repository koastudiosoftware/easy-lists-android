package com.easylists.presentation.ui.about

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easylists.presentation.R
import com.easylists.presentation.common.REPOSITORY_DONATE_URL
import com.easylists.presentation.common.SUPPORT_EMAIL
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.icons.MaterialIconsArrowBack
import com.easylists.presentation.icons.MaterialIconsEmail
import com.easylists.presentation.icons.Monetization_on
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController<Screen>,
    onLinkClick: (String) -> Unit,
    onEmailClick: (String, String) -> Unit,
    onPlayStoreClick: () -> Unit
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

        AboutScreenContent(innerPadding, onLinkClick, onEmailClick, onPlayStoreClick, navController)

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
            painter = rememberVectorPainter(MaterialIconsArrowBack),
            contentDescription = stringResource(R.string.return_to_previous_screen),
            modifier = Modifier.padding(start = MaterialTheme.spaces.mediumLarge),
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
    navController: NavController<Screen>,
) {
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
            Version(modifier = Modifier.fillMaxWidth())
        }

        item {
            Copyright(modifier = Modifier.fillMaxWidth())
        }

        item {
            Spacer(modifier = Modifier.size(MaterialTheme.spaces.large))
        }

        item {
            SectionTitle(
                modifier = Modifier.padding(MaterialTheme.spaces.large),
                title = stringResource(R.string.support),
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.medium)
            ) {
                SectionInfoItemAbout(
                    name = stringResource(R.string.email),
                    info = SUPPORT_EMAIL,
                    icon = MaterialIconsEmail,
                    showDivider = true,
                    onClick = {
                        onEmailClick(
                            SUPPORT_EMAIL,
                            "[Easy Lists App Support]"
                        )
                    }
                )
                SectionInfoItemAbout(
                    name = stringResource(R.string.donate),
                    info = REPOSITORY_DONATE_URL,
                    icon = Monetization_on,
                    showDivider = false,
                    onClick = {
                        onLinkClick(REPOSITORY_DONATE_URL)
                    }
                )
            }
        }

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
private fun Version(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.spaces.small),
            text = "v" + stringResource(id = R.string.app_version),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
//endregion


//region SectionInfoItemAbout
@Composable
fun SectionInfoItemAbout(
    name: String,
    info: String?,
    icon: ImageVector,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val showInfo = remember {
        derivedStateOf {
            info?.isNotBlank() == true
        }
    }

    Row(
        modifier = Modifier
            .clickable { onClick.invoke() }
            .padding(horizontal = MaterialTheme.spaces.large, vertical = MaterialTheme.spaces.medium)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Image(
            modifier = Modifier
                .size(size = MaterialTheme.spaces.extraLarge)
                .clip(shape = MaterialTheme.shapes.small),
            painter = rememberVectorPainter(icon),
            contentDescription = null,
        )
        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            if (showInfo.value) {
                Spacer(modifier = Modifier.size(MaterialTheme.spaces.small))
                Text(
                    text = info.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spaces.medium)
                .alpha(.2f),
        )
    }
}
//endregion
