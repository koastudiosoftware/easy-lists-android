package com.easylists.presentation.ui.invite

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.InvitePreview
import com.easylists.domain.models.InviteRole
import com.easylists.presentation.R
import com.easylists.presentation.models.JoinEvent
import com.easylists.presentation.models.JoinFailure
import com.easylists.presentation.models.JoinState
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.popUpTo

// The part of joining that people see. It sits at the root of the app, next to the
// NavHost, so its dialogs appear over whichever screen is showing.
@Composable
fun InviteJoinHost(
    viewModel: InviteJoinViewModel,
    navController: NavController<Screen>,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // A link was opened while signed out: take the person to the sign-in
    LaunchedEffect(state) {
        if (state is JoinState.NeedsSignIn &&
            navController.backstack.entries.last().destination != Screen.Settings
        ) {
            navController.navigate(Screen.Settings)
        }
    }

    // After joining, open the new list
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is JoinEvent.Joined -> {
                    navController.popUpTo { it == Screen.Lists }
                    navController.navigate(
                        Screen.ListItems(
                            listId = event.listId,
                            listName = event.listName,
                        )
                    )
                }
            }
        }
    }

    when (val current = state) {
        is JoinState.Confirm -> AlertDialog(
            onDismissRequest = viewModel::onDismiss,
            title = { Text(stringResource(R.string.join_list_title)) },
            text = { Text(confirmMessage(current.preview)) },
            confirmButton = {
                TextButton(onClick = viewModel::onJoin) {
                    Text(stringResource(R.string.join))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )

        JoinState.Loading, JoinState.Joining -> AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(MaterialTheme.spaces.large))
                    Text(stringResource(R.string.joining))
                }
            },
        )

        is JoinState.Failed -> AlertDialog(
            onDismissRequest = viewModel::onDismiss,
            text = { Text(stringResource(current.reason.messageRes())) },
            confirmButton = {
                TextButton(onClick = viewModel::onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            },
        )

        JoinState.Idle, JoinState.NeedsSignIn -> Unit
    }
}


@Composable
private fun confirmMessage(preview: InvitePreview): String {
    val inviterName = preview.inviterName
    val role = stringResource(
        when (preview.role) {
            InviteRole.Editor -> R.string.join_role_editor
            InviteRole.Viewer -> R.string.join_role_viewer
        }
    )
    return if (inviterName.isNullOrBlank()) {
        stringResource(R.string.join_list_message_unnamed, preview.listName, role)
    } else {
        stringResource(
            R.string.join_list_message_named,
            inviterName,
            preview.listName,
            role,
        )
    }
}

private fun JoinFailure.messageRes(): Int = when (this) {
    JoinFailure.NotFound -> R.string.join_failed_not_found
    JoinFailure.Expired -> R.string.join_failed_expired
    JoinFailure.WrongEmail -> R.string.join_failed_wrong_email
    JoinFailure.AlreadyMember -> R.string.join_failed_already_member
    JoinFailure.Other -> R.string.join_failed_other
}