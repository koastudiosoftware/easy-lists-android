package com.easylists.presentation.common.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easylists.presentation.BuildConfig
import com.easylists.presentation.R
import com.easylists.presentation.models.AccountUiModel
import com.easylists.presentation.ui.settings.SettingsViewModel
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel

@Composable
fun AccountCard(
    account: AccountUiModel?,
    isSigningIn: Boolean,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDebugSignIn: (String) -> Unit,
    onDebugSyncNow: () -> Unit,
) {
    CardGroup {
        if (account == null) {
            SettingsItem(
                textLine1 = stringResource(R.string.sign_in_with_google),
                textLine2 = stringResource(R.string.sign_in_description),
                onClick = { if (!isSigningIn) onSignInClick() },
                trailingContent = if (isSigningIn) {
                    { CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }
                } else null,
            )
        } else {
            SettingsItem(
                textLine1 = account.displayName ?: stringResource(R.string.signed_in),
                textLine2 = account.email ?: stringResource(R.string.signed_in_with_google),
                onClick = {},
            )
            HorizontalDivider()
            SettingsItem(
                textLine1 = stringResource(R.string.sign_out),
                textLine2 = stringResource(R.string.sign_out_description),
                onClick = onSignOutClick,
            )
        }
        if (BuildConfig.DEBUG) {
            HorizontalDivider()
            SettingsItem(
                textLine1 = "Debug: sign in as A",
                textLine2 = "uid debug-a",
                onClick = { onDebugSignIn("debug-a") },
            )
            HorizontalDivider()
            SettingsItem(
                textLine1 = "Debug: sign in as B",
                textLine2 = "uid debug-b",
                onClick = { onDebugSignIn("debug-b") },
            )
            HorizontalDivider()
            SettingsItem(
                textLine1 = "Debug: sign out",
                textLine2 = "runs the sign-out cleanup",
                onClick = onSignOutClick,
            )
            HorizontalDivider()
            SettingsItem(
                textLine1 = "Debug: sync now",
                textLine2 = "pushes every dirty row",
                onClick = onDebugSyncNow,
            )
        }
    }
}
