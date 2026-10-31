package com.easylists.presentation.ui.members

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.InviteRole
import com.easylists.domain.models.ListMember
import com.easylists.domain.models.ListRole
import com.easylists.domain.models.PendingInvite
import com.easylists.presentation.R
import com.easylists.presentation.common.MembersError
import com.easylists.presentation.models.InvitesState
import com.easylists.presentation.ui.theme.spaces
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel

// Who is on a list. The owner can switch people between editor and viewer,
// remove them, and revoke invites that haven't been used. Everyone else can
// see the people and leave.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersSheet(
    listId: String,
    listName: String,
    onDismiss: () -> Unit,
    // called after this person has left the list
    onLeft: () -> Unit,
    viewModel: MembersViewModel = hiltViewModel(),
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val access by viewModel.access.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val canManage = access?.canManageList == true
    val canLeave = access != null && !canManage

    LaunchedEffect(listId) { viewModel.init(listId) }
    LaunchedEffect(canManage) { if (canManage) viewModel.loadInvites() }
    LaunchedEffect(Unit) { viewModel.left.collect { onLeft() } }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(bottom = MaterialTheme.spaces.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.medium),
        ) {
            Text(
                text = stringResource(R.string.members_title, listName),
                style = MaterialTheme.typography.titleLarge,
            )

            members.forEach { member ->
                MemberRow(
                    member = member,
                    canManage = canManage,
                    canLeave = canLeave,
                    isWorking = ui.isWorking,
                    onToggleRole = { viewModel.onToggleRole(member) },
                    onRemove = { viewModel.onRemoveRequested(member) },
                    onLeave = viewModel::onLeaveRequested
                )
            }

            if (canManage) {
                InvitesSection(
                    state = ui.invites,
                    onRevoke = viewModel::onRevoke,
                    onRetry = viewModel::loadInvites,
                )
            }

            ui.error?.let { error ->
                Text(
                    text = stringResource(
                        when (error) {
                            MembersError.LeaveFailed -> R.string.members_leave_failed
                            MembersError.RevokeFailed -> R.string.members_revoke_failed
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    ui.confirmRemove?.let { member ->
        AlertDialog(
            onDismissRequest = viewModel::onRemoveCancelled,
            title = { Text(stringResource(R.string.members_remove_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.members_remove_message,
                        member.nameOrFallback(),
                        listName,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onRemoveConfirmed) {
                    Text(stringResource(R.string.members_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onRemoveCancelled) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (ui.confirmLeave) {
        AlertDialog(
            onDismissRequest = viewModel::onLeaveCancelled,
            title = { Text(stringResource(R.string.members_leave_title)) },
            text = { Text(stringResource(R.string.members_leave_message, listName)) },
            confirmButton = {
                TextButton(onClick = viewModel::onLeaveConfirmed) {
                    Text(stringResource(R.string.members_leave))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onLeaveCancelled) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}


@Composable
private fun MemberRow(
    member: ListMember,
    canManage: Boolean,
    canLeave: Boolean,
    isWorking: Boolean,
    onToggleRole: () -> Unit,
    onRemove: () -> Unit,
    onLeave: () -> Unit = {},
) {
    val roleLabel = stringResource(member.role.labelRes())
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.nameOrFallback(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (member.isYou) {
                    stringResource(R.string.members_role_you, roleLabel)
                } else {
                    roleLabel
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // The owner changes everyone's role except their own
        if (canManage && member.role != ListRole.Owner) {
            AssistChip(onClick = onToggleRole, label = { Text(roleLabel) })
            TextButton(onClick = onRemove) {
                Text(stringResource(R.string.members_remove))
            }
        }
        // The editor/viewer can leave lists, owners cannot
        if (canLeave && member.role != ListRole.Owner) {
            AssistChip(
                onClick = onLeave,
                enabled = !isWorking,
                label = {
                    if (isWorking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.members_leave))
                    }
                }
            )
        }
    }
}


@Composable
private fun InvitesSection(
    state: InvitesState,
    onRevoke: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Text(
        text = stringResource(R.string.members_invites_title),
        style = MaterialTheme.typography.titleMedium,
    )
    when (state) {
        InvitesState.Loading -> CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )

        InvitesState.Failed -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.members_invites_failed),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.members_retry))
            }
        }

        is InvitesState.Loaded -> {
            if (state.invites.isEmpty()) {
                Text(
                    text = stringResource(R.string.members_invites_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.invites.forEach { invite ->
                InviteRow(invite = invite, onRevoke = { onRevoke(invite.token) })
            }
        }
    }
}


@Composable
private fun InviteRow(invite: PendingInvite, onRevoke: () -> Unit) {
    val role = stringResource(
        when (invite.role) {
            InviteRole.Editor -> R.string.share_role_editor
            InviteRole.Viewer -> R.string.share_role_viewer
        }
    )
    val expires = DateUtils.getRelativeTimeSpanString(
        invite.expiresAtMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = role, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.members_invite_expires, expires),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            invite.email?.let { email ->
                Text(
                    text = stringResource(
                        R.string.members_invite_only_for,
                        email,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = onRevoke) {
            Text(stringResource(R.string.members_revoke))
        }
    }
}


private fun ListRole.labelRes(): Int = when (this) {
    ListRole.Owner -> R.string.members_role_owner
    ListRole.Editor -> R.string.share_role_editor
    ListRole.Viewer -> R.string.share_role_viewer
}


// A missing or blank name falls back to a generic label
@Composable
private fun ListMember.nameOrFallback(): String =
    displayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.members_unnamed)
