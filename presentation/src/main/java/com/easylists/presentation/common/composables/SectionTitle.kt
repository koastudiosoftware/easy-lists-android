package com.easylists.presentation.common.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.easylists.presentation.ui.theme.spaces

@Composable
fun SectionTitle(
    title: String,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable{ onClick.invoke() }
        }
    ) {
        Column(
            modifier = Modifier
                .weight(3f)
                .padding(  // padding is determined by the caller
                    horizontal = MaterialTheme.spaces.none,
                    vertical = MaterialTheme.spaces.none
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                modifier = modifier.semantics { heading() },
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleLarge
            )
        }
        if (icon != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(  // padding is determined by the caller
                        horizontal = MaterialTheme.spaces.none,
                        vertical = MaterialTheme.spaces.none
                    ),
                horizontalAlignment = Alignment.End
            ) {
                icon()
            }
        }
    }
}
