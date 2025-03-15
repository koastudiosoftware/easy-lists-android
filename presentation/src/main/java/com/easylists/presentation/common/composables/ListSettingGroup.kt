package com.easylists.presentation.common.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.easylists.presentation.ui.settings.SettingsViewModel
import com.easylists.presentation.ui.theme.spaces
import com.toxicbakery.logging.Arbor

@Composable
fun <E : Enum<E>> ListSettingGroup(
    title: String,
    options: List<E>,
    index: Int,
    viewModel: SettingsViewModel
) {
    var showList = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = MaterialTheme.spaces.none)
            .padding(top = MaterialTheme.spaces.none)
            .clickable {
                showList.value = !showList.value
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = MaterialTheme.spaces.large)
                .padding(top = MaterialTheme.spaces.medium)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!showList.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spaces.large)
                ) {
                    Text(text = viewModel.listSettingsSelected(options[index]))
                }
            }
        }
        if (showList.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spaces.medium)
            ) {
                options.forEach { option ->
                    ListSettingItem(
                        option = option.toString(),
                        selected = viewModel.listSettingsSelected(option),
                        onClick = {
                            viewModel.onListSettingsChanged(option)
                            showList.value = !showList.value
                        }
                    )
                }
            }
        }
    }
}