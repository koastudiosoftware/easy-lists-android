package com.easylists.presentation.common.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.easylists.presentation.common.appKeyboardOptions
import com.easylists.presentation.ui.theme.spaces

@Composable
fun KeyboardOptionsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spaces.medium),
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = singleLine,
        maxLines = 1,
        keyboardOptions = appKeyboardOptions(keyboardType, imeAction),
        isError = isError,
        supportingText = {
            if (errorMessage.isNotEmpty()) Text(text = errorMessage)
        },
    )
}