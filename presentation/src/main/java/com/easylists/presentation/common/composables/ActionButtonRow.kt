package com.easylists.presentation.common.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.easylists.presentation.ui.theme.spaces

@Composable
fun ActionRowButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    destructive: Boolean = false,
    showDivider: Boolean = true,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(top = MaterialTheme.spaces.medium, bottom = MaterialTheme.spaces.none),
            contentAlignment = Alignment.Center,
        ) {
            FilledTonalButton(
                modifier = Modifier.widthIn(min = 240.dp),
                onClick = onClick,
                enabled = enabled,
                colors = if (destructive) {
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                } else {
                    ButtonDefaults.filledTonalButtonColors()
                },
            ) {
                Text(text = text)
            }
        }
        if (showDivider) HorizontalDivider()
    }
}