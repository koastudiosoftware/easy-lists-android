package com.easylists.presentation.common.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.easylists.presentation.icons.MaterialIconsEdit
import com.easylists.presentation.icons.MaterialIconsExpandLess
import com.easylists.presentation.icons.MaterialIconsExpandMore
import com.easylists.presentation.ui.theme.spaces

@Composable
fun <T> ListSettingGroup(
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = MaterialTheme.spaces.medium)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    text = title,
                )
                if (!expanded) {
                    Text(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        text = optionLabel(selected),
                    )
                }
            }
            Icon(
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
                imageVector = if (expanded) MaterialIconsExpandLess else MaterialIconsExpandMore,
                contentDescription = null, // the whole row is the click target
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spaces.medium)
            ) {
                options.forEach { option ->
                    ListSettingItem(
                        option = optionLabel(option),
                        selected = optionLabel(selected),
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}