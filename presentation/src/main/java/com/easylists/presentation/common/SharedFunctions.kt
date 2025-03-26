package com.easylists.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.math.pow


//region isNumeric
fun isNumeric(str: String): Boolean = str
    .removePrefix("-")
    .removePrefix("+")
    .all { it in '0'..'9' }
//endregion


//region Color.toHexCodeWithAlpha()
fun Color.toHexCodeWithAlpha(): String {
    val alpha = this.alpha * 255
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format(
        "#%02x%02x%02x%02x",
        alpha.toInt(),
        red.toInt(),
        green.toInt(),
        blue.toInt()
    )
}
//endregion


//region Color.getContrastColor()
@Composable
fun Color.getContrastColor(): Color {
    val flipRelativeLuminance = 0.342  // based on APCA™ 0.98G middle contrast BG color

    val trc = 2.4

    val redCoefficient = 0.2126729
    val greenCoefficient = 0.7151522
    val blueCoefficient = 0.0721750

    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255

    val relativeLuminance = (red / 255.0).pow(trc) * redCoefficient +
            (green / 255.0).pow(trc) * greenCoefficient +
            (blue / 255.0).pow(trc) * blueCoefficient

    return if (relativeLuminance < flipRelativeLuminance) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
}
//endregion
