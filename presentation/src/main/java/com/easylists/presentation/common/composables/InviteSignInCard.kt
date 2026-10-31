package com.easylists.presentation.common.composables


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easylists.domain.common.ViewMode
import com.easylists.presentation.R
import com.easylists.presentation.ui.theme.spaces

// Shown at the top of Settings when an invitation link was opened while nobody is
// signed in. It has the sign-in button itself, so the person doesn't have to
// find it further down the screen.
@Composable
fun InviteSignInCard(
    isSigningIn: Boolean,
    onSignInClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spaces.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.medium),
        ) {
            Text(
                text = stringResource(R.string.invite_sign_in_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.invite_sign_in_message),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.invite_not_now))
                }
                Spacer(Modifier.width(MaterialTheme.spaces.small))
                Button(onClick = onSignInClick, enabled = !isSigningIn) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.sign_in_with_google))
                    }
                }
            }
        }
    }
}
