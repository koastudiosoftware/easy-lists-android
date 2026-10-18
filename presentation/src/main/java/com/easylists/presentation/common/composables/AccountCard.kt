package com.easylists.presentation.common.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easylists.presentation.R
import com.easylists.presentation.models.AccountUiModel

@Composable
fun AccountCard(
    account: AccountUiModel?,   // null = signed out
    isSigningIn: Boolean,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
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
    }
}
