package com.easylists.presentation.common.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.ui.theme.spaces

/** A settings row with a trailing switch. Tapping anywhere on the row toggles it. */
@Composable
fun ToggleSettingsItem(
    textLine1: String,
    textLine2: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingItemLayout(
        textLine1 = textLine1,
        textLine2 = textLine2,
        onClick = { onCheckedChange(!enabled) },
    ) {
        // The whole row is the tap target, so the switch doesn't need its own 48dp
        // minimum, which would make toggle rows taller than the other settings rows.
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            Switch(
                checked = enabled,
                onCheckedChange = onCheckedChange,
                thumbContent = if (enabled) {
                    {
                        Icon(
                            imageVector = MaterialIconsCheck,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}


/** A plain settings row with no trailing control, for actions and navigation. */
@Composable
fun SettingsItem(
    textLine1: String,
    textLine2: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    SettingItemLayout(
        textLine1 = textLine1,
        textLine2 = textLine2,
        onClick = onClick,
        trailingContent,
    )
}


/** Shared layout: two lines of text on the left and an optional trailing control on the right. */
@Composable
private fun SettingItemLayout(
    textLine1: String,
    textLine2: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = MaterialTheme.spaces.large,
                vertical = MaterialTheme.spaces.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                text = textLine1,
            )
            Text(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                text = textLine2,
            )
        }
        if (trailingContent != null) {
            Spacer(Modifier.width(MaterialTheme.spaces.medium))
            trailingContent()
        }
    }
}
