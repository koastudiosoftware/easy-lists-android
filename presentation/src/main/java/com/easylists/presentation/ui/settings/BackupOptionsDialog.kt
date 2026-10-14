package com.easylists.presentation.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.easylists.presentation.R
import com.easylists.presentation.ui.theme.spaces

enum class BackupContent(@StringRes val labelRes: Int) {
    Settings(R.string.settings),
    ListsAndItems(R.string.lists_and_items),
    Photos(R.string.photos),
    Categories(R.string.categories),
    Tags(R.string.tags),
}

@Composable
fun BackupOptionsDialog(
    selected: Set<BackupContent>,
    onSelectedChange: (Set<BackupContent>) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_options_title)) },
        text = {
            Column {
                BackupContent.entries.forEach { content ->
                    val checked = content in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = checked,
                                role = Role.Checkbox,
                                onValueChange = { isChecked ->
                                    onSelectedChange(
                                        if (isChecked) selected + content else selected - content
                                    )
                                },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // The whole row handles the tap, so the checkbox itself is display-only.
                        Checkbox(checked = checked, onCheckedChange = null)
                        Spacer(Modifier.width(MaterialTheme.spaces.medium))
                        Text(stringResource(content.labelRes))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        },
    )
}