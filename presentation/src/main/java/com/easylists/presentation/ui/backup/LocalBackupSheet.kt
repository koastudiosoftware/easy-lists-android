package com.easylists.presentation.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.easylists.data.backup.BackupSection
import com.easylists.presentation.R
import com.easylists.presentation.common.composables.SettingsCardGroup
import com.easylists.presentation.icons.MaterialIconsKeyboardArrowRight
import com.easylists.presentation.models.BackupMessage
import com.easylists.presentation.models.PendingRestore
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel

private const val MAX_LISTED_BACKUPS = 10

/**
 * The whole Local backup UI as a bottom sheet: pick a folder, choose what to include, back up now,
 * and restore from the folder or from any backup file. Show it from the "Local backup" row:
 *
 *     var showLocalBackup by remember { mutableStateOf(false) }
 *     ...row onClick = { showLocalBackup = true }
 *     if (showLocalBackup) LocalBackupSheet(onDismiss = { showLocalBackup = false })
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalBackupSheet(
    onDismiss: () -> Unit,
    viewModel: LocalBackupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Messages appear at the top of the sheet, so bring the top into view whenever one arrives
    LaunchedEffect(state.message) {
        if (state.message != null) scrollState.animateScrollTo(0)
    }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) viewModel.onFolderPicked(uri)
    }
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onBackupChosen(uri)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.local_backups),
                style = MaterialTheme.typography.titleLarge,
            )

            if (state.isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            state.message?.let { BackupMessageBanner(it) }

            //region Back up
            Text(
                text = stringResource(R.string.backup_create_header),
                style = MaterialTheme.typography.titleMedium,
            )

            SettingsCardGroup {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.backup_folder_label),
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Text(
                                text = state.folderLabel ?: stringResource(R.string.backup_folder_not_set),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        OutlinedButton(
                            onClick = { folderLauncher.launch(null) },
                            enabled = !state.isBusy,
                        ) {
                            Text(
                                stringResource(
                                    if (state.hasFolder) R.string.backup_change_folder
                                    else R.string.backup_choose_folder
                                )
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.backup_include),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    BackupSection.entries.forEach { section ->
                        SectionRow(
                            label = stringResource(section.labelRes()),
                            checked = section in state.sections,
                            enabled = !state.isBusy,
                            onToggle = { viewModel.toggleSection(section) },
                        )
                    }

                    Button(
                        onClick = viewModel::createBackup,
                        enabled = state.hasFolder && state.sections.isNotEmpty() && !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.backup_now))
                    }
                    if (!state.hasFolder) {
                        Text(
                            text = stringResource(R.string.backup_choose_folder_first),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else if (state.sections.isEmpty()) {
                        Text(
                            text = stringResource(R.string.backup_pick_something),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            HorizontalDivider()

            //region Restore
            Text(
                text = stringResource(R.string.backup_restore_header),
                style = MaterialTheme.typography.titleMedium,
            )

            if (state.hasFolder) {
                Text(
                    text = stringResource(R.string.backup_in_folder),
                    style = MaterialTheme.typography.labelLarge,
                )
                if (state.backups.isEmpty()) {
                    Text(
                        text = stringResource(R.string.backup_no_backups),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    SettingsCardGroup {
                        state.backups.take(MAX_LISTED_BACKUPS).forEachIndexed { index, entry ->
                            if (index > 0) {
                                HorizontalDivider()
                            }
                            ListItem(
                                headlineContent = {
                                    Text(
                                        if (entry.lastModified > 0) formatDate(entry.lastModified)
                                        else entry.name
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        android.text.format.Formatter.formatShortFileSize(context, entry.size)
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = MaterialIconsKeyboardArrowRight,
                                        contentDescription = null,
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.clickable(enabled = !state.isBusy) {
                                    viewModel.onBackupChosen(entry.uri)
                                },
                            )
                        }
                    }
                    if (state.backups.size > MAX_LISTED_BACKUPS) {
                        Text(
                            text = stringResource(R.string.backup_showing_newest, MAX_LISTED_BACKUPS),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Works with or without a folder, e.g. right after a reinstall
            OutlinedButton(
                onClick = { fileLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                enabled = !state.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.backup_choose_file))
            }
            //endregion
        }
    }

    state.pendingRestore?.let { pending ->
        RestoreConfirmDialog(
            pending = pending,
            onConfirm = viewModel::confirmRestore,
            onCancel = viewModel::cancelRestore,
        )
    }
}

@Composable
private fun RestoreConfirmDialog(
    pending: PendingRestore,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val summary = pending.summary
    val sectionNames = summary.sections.map { stringResource(it.labelRes()) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.backup_restore_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summary.createdTimestamp?.let {
                    Text(stringResource(R.string.backup_restore_made, formatDate(it)))
                }
                Text(stringResource(R.string.backup_restore_contains, sectionNames.joinToString(", ")))
                Text(
                    stringResource(
                        R.string.backup_restore_counts,
                        summary.lists,
                        summary.listItems,
                        summary.categories,
                        summary.tags,
                        summary.photos,
                    )
                )
                Text(stringResource(R.string.backup_restore_warning))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.backup_restore_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.backup_cancel))
            }
        },
    )
}

@Composable
private fun BackupMessageBanner(message: BackupMessage) {
    val container = if (message.isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val content = if (message.isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(message.textRes, *message.args.toTypedArray()),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun SectionRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@StringRes
private fun BackupSection.labelRes(): Int = when (this) {
    BackupSection.Settings -> R.string.backup_section_settings
    BackupSection.Lists -> R.string.backup_section_lists
    BackupSection.Categories -> R.string.backup_section_categories
    BackupSection.Tags -> R.string.backup_section_tags
    BackupSection.Photos -> R.string.backup_section_photos
}

private fun formatDate(millis: Long): String =
    java.text.DateFormat
        .getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
        .format(java.util.Date(millis))