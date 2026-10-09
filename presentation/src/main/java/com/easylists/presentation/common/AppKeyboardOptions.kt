package com.easylists.presentation.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.easylists.domain.common.Capitalization

val LocalCapitalization = compositionLocalOf { Capitalization.NoCapitalization }

fun Capitalization.toKeyboardCapitalization(): KeyboardCapitalization = when (this) {
    Capitalization.NoCapitalization -> KeyboardCapitalization.None
    Capitalization.CapitalizeFirstLetter -> KeyboardCapitalization.Sentences
    Capitalization.CapitalizeAllWords -> KeyboardCapitalization.Words
}

@Composable
fun appKeyboardOptions(
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
): KeyboardOptions {
    val capitalization = LocalCapitalization.current
    return remember(capitalization, keyboardType, imeAction) {
        KeyboardOptions(
            capitalization = capitalization.toKeyboardCapitalization(),
            keyboardType = keyboardType,
            imeAction = imeAction,
            showKeyboardOnFocus = true,
        )
    }
}