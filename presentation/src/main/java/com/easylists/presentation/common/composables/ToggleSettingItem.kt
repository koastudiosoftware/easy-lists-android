package com.easylists.presentation.common.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.easylists.presentation.icons.MaterialIconsCheck
import com.easylists.presentation.ui.theme.spaces

@Composable
fun ToggleSettingItem(
    textLine1: String,
    textLine2: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {

    Row(
        modifier = Modifier.selectableGroup()
            .clickable { onCheckedChange(!enabled) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = MaterialTheme.spaces.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(3f)
                    .padding(horizontal = MaterialTheme.spaces.large)
                    .padding(top = MaterialTheme.spaces.medium)
            ) {
                Text(
                    fontWeight = FontWeight.Bold,
                    text = textLine1,
                )
                Text(
                    text = textLine2,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spaces.large)
                )
            }
            Column(
                modifier = Modifier.weight(0.65f)
                    .padding(horizontal = MaterialTheme.spaces.large),
                horizontalAlignment = Alignment.End,
            ) {
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
}
