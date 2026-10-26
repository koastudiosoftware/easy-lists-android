package com.easylists.presentation.ui.share

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.InviteRole
import com.easylists.presentation.R
import com.easylists.presentation.common.composables.AppTextField
import com.easylists.presentation.common.composables.SectionTitle
import com.easylists.presentation.models.ShareError
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel

// The sheet an owner uses to share a list: pick a role, optionally lock the
// invite to one email, create the link, and send it through Android's share sheet.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareListSheet(
    listId: String,
    listName: String,
    onDismiss: () -> Unit,
    viewModel: ShareListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signedIn by viewModel.signedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(listId) { viewModel.reset() }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(bottom = MaterialTheme.spaces.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.medium),
        ) {
            Text(
                text = stringResource(R.string.share_list_title, listName),
                style = MaterialTheme.typography.titleLarge,
            )

            if (signedIn == false) {
                Text(
                    text = stringResource(R.string.share_sign_in_required),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            //region Role
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                InviteRole.entries.forEachIndexed { index, role ->
                    SegmentedButton(
                        selected = state.role == role,
                        onClick = { viewModel.onRoleChange(role) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = InviteRole.entries.size,
                        ),
                    ) {
                        Text(stringResource(role.labelRes()))
                    }
                }
            }
            Text(
                text = stringResource(state.role.hintRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            //endregion

            //region Email
            AppTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.share_email_label),
                isError = state.error == ShareError.InvalidEmail,
                errorMessage = if (state.error == ShareError.InvalidEmail) {
                    stringResource(R.string.share_email_invalid)
                } else "",
            )
            Text(
                text = stringResource(R.string.share_email_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            //endregion

            //region Create and send
            Button(
                onClick = { viewModel.createLink(listId) },
                enabled = signedIn == true && !state.isWorking,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isWorking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.share_create_link))
                }
            }

            if (state.error == ShareError.Failed) {
                Text(
                    text = stringResource(R.string.share_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            state.link?.let { link ->
                Text(
                    text = stringResource(R.string.share_link_ready),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = link,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { sendLink(context, listName, link) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.share_send_link))
                }
            }
            //endregion
        }
    }
}


private fun InviteRole.labelRes(): Int = when (this) {
    InviteRole.Editor -> R.string.share_role_editor
    InviteRole.Viewer -> R.string.share_role_viewer
}


private fun InviteRole.hintRes(): Int = when (this) {
    InviteRole.Editor -> R.string.share_role_editor_hint
    InviteRole.Viewer -> R.string.share_role_viewer_hint
}


// Hands the link to Android's share sheet: text message, email, any app
private fun sendLink(context: Context, listName: String, link: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, listName)
        putExtra(
            Intent.EXTRA_TEXT,
            context.getString(R.string.share_message, listName, link),
        )
    }
    context.startActivity(Intent.createChooser(send, null))
}
