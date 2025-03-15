package com.easylists.presentation.ui.theme

import androidx.compose.material3.CardColors
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Solarized color palette
val SolarizedBase3 = Color(0xFFFDF6E3)
val SolarizedBase2 = Color(0xFFEEE8D5)
val SolarizedBase1 = Color(0xFF93A1A1)
val SolarizedBase0 = Color(0xFF839496)
val SolarizedBase00 = Color(0xFF657B83)
val SolarizedBase01 = Color(0xFF586E75)
val SolarizedBase02 = Color(0xFF073642)
val SolarizedBase03 = Color(0xFF002b36)
val SolarizedYellow = Color(0xFFB58900)
val SolarizedOrange = Color(0xFFCB4b16)
val SolarizedRed = Color(0xFFDC322F)
val SolarizedMagenta = Color(0xFFD33682)
val SolarizedViolet = Color(0xFF6C71C4)
val SolarizedBlue = Color(0xFF268BD2)
val SolarizedCyan = Color(0xFF2AA198)
val SolarizedGreen = Color(0xFF859900)

// 20% opacity
val SolarizedBase1_20 = Color(0x3393A1A1)

// 60% opacity
val SolarizedBase01_60 = Color(0x99586E75)
val SolarizedBase02_60 = Color(0x99073642)
val SolarizedBase2_60 = Color(0x99EEE8D5)

// 80% opacity
val SolarizedGreen_80 = Color(0xCC859900)
val SolarizedGreenYellow_80 = Color(0xCC9D9100)
val SolarizedYellow_80 = Color(0xCCB58900)
val SolarizedYellowRed_80 = Color(0xCCC95E18)
val SolarizedRed_80 = Color(0xCCDC322F)

// Integer-based colors (for CandleStick chart)
const val SolarizedGreenInt = 0xFF859900.toInt()
const val SolarizedYellowInt = 0xFFB58900.toInt()
const val SolarizedRedInt = 0xFFdc322f.toInt()

// colors that aren't in the palette but are used in the app
// these colors fall between the named palette colors
val SolarizedGreenYellow = Color(0xFF9D9100)
val SolarizedYellowRed = Color(0xFFC95E18)


// individual colors universal to both light and dark themes
val ColorScheme.earningsEstimate: Color @Composable
get() = SolarizedBase1
val ColorScheme.gain: Color @Composable
get() = SolarizedGreen
val ColorScheme.link: Color @Composable
get() = SolarizedBlue
val ColorScheme.loss: Color @Composable
get() = SolarizedRed
val ColorScheme.tooltipContainer: Color @Composable
get() = SolarizedViolet

val defaultCardColorsLight = CardColors(
    containerColor = SolarizedBase2,
    contentColor = SolarizedBase01,
    disabledContainerColor = SolarizedBase01,
    disabledContentColor = SolarizedBase1
)

val defaultCardColorsDark = CardColors(
    containerColor = SolarizedBase02,
    contentColor = SolarizedBase1,
    disabledContainerColor = SolarizedBase1,
    disabledContentColor = SolarizedBase01
)

val primaryLight = SolarizedViolet                      // bottom nav background, focused OutlinedTextBox border
val onPrimaryLight = SolarizedBase3                     // text on cards
val primaryContainerLight = SolarizedViolet
val onPrimaryContainerLight = SolarizedBase3
val secondaryLight = Color(0xFF6E4AAD)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = SolarizedBase3            // selected nav button background
val onSecondaryContainerLight = SolarizedBase01
val tertiaryLight = Color(0xFF960082)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = SolarizedCyan
val onTertiaryContainerLight = SolarizedBase3
val errorLight = SolarizedRed
val onErrorLight = SolarizedBase3
val errorContainerLight = SolarizedRed
val onErrorContainerLight = SolarizedBase3
val backgroundLight = SolarizedBase3                    // background
val onBackgroundLight = SolarizedBase01                 // card text color
val surfaceLight = SolarizedBase3                       // surface background
val onSurfaceLight = SolarizedBase01                    // screen title text, focused OutlinedTextBox text
val surfaceVariantLight = SolarizedBase3                // card background
val onSurfaceVariantLight = SolarizedBase01             // unselected bottom nav text. text on cards color
val outlineLight = SolarizedBase01                      // unfocused OutlinedTextBox border
val outlineVariantLight = SolarizedBase2                // horizontal divider
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF332E3A)
val inverseOnSurfaceLight = Color(0xFFF7EDFE)
val inversePrimaryLight = Color(0xFFD4BBFF)
val surfaceDimLight = Color(0xFFE0D7E7)
val surfaceBrightLight = Color(0xFFFEF7FF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = SolarizedBase3           // bottom sheet background color
val surfaceContainerLight = SolarizedBase2              // overflow menu background color
val surfaceContainerHighLight = SolarizedBase2
val surfaceContainerHighestLight = SolarizedBase3       // card background color

val primaryLightMediumContrast = Color(0xFF48009A)
val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
val primaryContainerLightMediumContrast = Color(0xFF832CFF)
val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val secondaryLightMediumContrast = Color(0xFF441D82)
val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
val secondaryContainerLightMediumContrast = Color(0xFF7D59BD)
val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryLightMediumContrast = Color(0xFF68005A)
val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightMediumContrast = Color(0xFFBE0BA5)
val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val errorLightMediumContrast = Color(0xFF740006)
val onErrorLightMediumContrast = Color(0xFFFFFFFF)
val errorContainerLightMediumContrast = Color(0xFFCF2C27)
val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
val backgroundLightMediumContrast = Color(0xFFFEF7FF)
val onBackgroundLightMediumContrast = Color(0xFF1E1A25)
val surfaceLightMediumContrast = Color(0xFFFEF7FF)
val onSurfaceLightMediumContrast = Color(0xFF130F1A)
val surfaceVariantLightMediumContrast = Color(0xFFE9DEF6)
val onSurfaceVariantLightMediumContrast = Color(0xFF3A3345)
val outlineLightMediumContrast = Color(0xFF574F62)
val outlineVariantLightMediumContrast = Color(0xFF726A7D)
val scrimLightMediumContrast = Color(0xFF000000)
val inverseSurfaceLightMediumContrast = Color(0xFF332E3A)
val inverseOnSurfaceLightMediumContrast = Color(0xFFF7EDFE)
val inversePrimaryLightMediumContrast = Color(0xFFD4BBFF)
val surfaceDimLightMediumContrast = Color(0xFFCCC3D3)
val surfaceBrightLightMediumContrast = Color(0xFFFEF7FF)
val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightMediumContrast = Color(0xFFF9F0FF)
val surfaceContainerLightMediumContrast = Color(0xFFEEE5F5)
val surfaceContainerHighLightMediumContrast = Color(0xFFE2DAEA)
val surfaceContainerHighestLightMediumContrast = Color(0xFFD7CEDE)

val primaryLightHighContrast = Color(0xFF3B0081)
val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
val primaryContainerLightHighContrast = Color(0xFF5F00C9)
val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
val secondaryLightHighContrast = Color(0xFF3A0D77)
val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
val secondaryContainerLightHighContrast = Color(0xFF583396)
val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
val tertiaryLightHighContrast = Color(0xFF57004A)
val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightHighContrast = Color(0xFF890077)
val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
val errorLightHighContrast = Color(0xFF600004)
val onErrorLightHighContrast = Color(0xFFFFFFFF)
val errorContainerLightHighContrast = Color(0xFF98000A)
val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
val backgroundLightHighContrast = Color(0xFFFEF7FF)
val onBackgroundLightHighContrast = Color(0xFF1E1A25)
val surfaceLightHighContrast = Color(0xFFFEF7FF)
val onSurfaceLightHighContrast = Color(0xFF000000)
val surfaceVariantLightHighContrast = Color(0xFFE9DEF6)
val onSurfaceVariantLightHighContrast = Color(0xFF000000)
val outlineLightHighContrast = Color(0xFF30293A)
val outlineVariantLightHighContrast = Color(0xFF4D4659)
val scrimLightHighContrast = Color(0xFF000000)
val inverseSurfaceLightHighContrast = Color(0xFF332E3A)
val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
val inversePrimaryLightHighContrast = Color(0xFFD4BBFF)
val surfaceDimLightHighContrast = Color(0xFFBEB6C5)
val surfaceBrightLightHighContrast = Color(0xFFFEF7FF)
val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightHighContrast = Color(0xFFF7EDFE)
val surfaceContainerLightHighContrast = Color(0xFFE8DFEF)
val surfaceContainerHighLightHighContrast = Color(0xFFDAD1E1)
val surfaceContainerHighestLightHighContrast = Color(0xFFCCC3D3)

val primaryDark = SolarizedCyan
val onPrimaryDark = SolarizedBase2
val primaryContainerDark = SolarizedCyan
val onPrimaryContainerDark = SolarizedBase2
val secondaryDark = Color(0xFFD4BBFF)
val onSecondaryDark = Color(0xFF3E147C)
val secondaryContainerDark = SolarizedBase03
val onSecondaryContainerDark = SolarizedBase1
val tertiaryDark = Color(0xFFFFADE5)
val onTertiaryDark = Color(0xFF5E0051)
val tertiaryContainerDark = SolarizedViolet
val onTertiaryContainerDark = SolarizedBase2
val errorDark = SolarizedRed
val onErrorDark = SolarizedBase3
val errorContainerDark = SolarizedRed
val onErrorContainerDark = SolarizedBase3
val backgroundDark = SolarizedBase03
val onBackgroundDark = SolarizedBase1
val surfaceDark = SolarizedBase03
val onSurfaceDark = SolarizedBase1
val surfaceVariantDark = SolarizedBase03
val onSurfaceVariantDark = SolarizedBase1
val outlineDark = SolarizedBase1
val outlineVariantDark = SolarizedBase02
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE8DFEF)
val inverseOnSurfaceDark = Color(0xFF332E3A)
val inversePrimaryDark = Color(0xFF7917F5)
val surfaceDimDark = Color(0xFF15111C)
val surfaceBrightDark = Color(0xFF3C3743)
val surfaceContainerLowestDark = Color(0xFF100C17)
val surfaceContainerLowDark = SolarizedBase03
val surfaceContainerDark = SolarizedBase02
val surfaceContainerHighDark = SolarizedBase02
val surfaceContainerHighestDark = SolarizedBase03

val primaryDarkMediumContrast = Color(0xFFE6D5FF)
val onPrimaryDarkMediumContrast = Color(0xFF330071)
val primaryContainerDarkMediumContrast = Color(0xFFA675FF)
val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
val secondaryDarkMediumContrast = Color(0xFFE6D5FF)
val onSecondaryDarkMediumContrast = Color(0xFF330071)
val secondaryContainerDarkMediumContrast = Color(0xFFA27EE4)
val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
val tertiaryDarkMediumContrast = Color(0xFFFFCEED)
val onTertiaryDarkMediumContrast = Color(0xFF4C0041)
val tertiaryContainerDarkMediumContrast = Color(0xFFF149D2)
val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
val errorDarkMediumContrast = Color(0xFFFFD2CC)
val onErrorDarkMediumContrast = Color(0xFF540003)
val errorContainerDarkMediumContrast = Color(0xFFFF5449)
val onErrorContainerDarkMediumContrast = Color(0xFF000000)
val backgroundDarkMediumContrast = Color(0xFF15111C)
val onBackgroundDarkMediumContrast = Color(0xFFE8DFEF)
val surfaceDarkMediumContrast = Color(0xFF15111C)
val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkMediumContrast = Color(0xFF4B4456)
val onSurfaceVariantDarkMediumContrast = Color(0xFFE3D8EF)
val outlineDarkMediumContrast = Color(0xFFB8AEC4)
val outlineVariantDarkMediumContrast = Color(0xFF968DA2)
val scrimDarkMediumContrast = Color(0xFF000000)
val inverseSurfaceDarkMediumContrast = Color(0xFFE8DFEF)
val inverseOnSurfaceDarkMediumContrast = Color(0xFF2D2834)
val inversePrimaryDarkMediumContrast = Color(0xFF5E00C6)
val surfaceDimDarkMediumContrast = Color(0xFF15111C)
val surfaceBrightDarkMediumContrast = Color(0xFF47424F)
val surfaceContainerLowestDarkMediumContrast = Color(0xFF090610)
val surfaceContainerLowDarkMediumContrast = Color(0xFF201C27)
val surfaceContainerDarkMediumContrast = Color(0xFF2A2632)
val surfaceContainerHighDarkMediumContrast = Color(0xFF35303D)
val surfaceContainerHighestDarkMediumContrast = Color(0xFF413B48)

val primaryDarkHighContrast = Color(0xFFF6ECFF)
val onPrimaryDarkHighContrast = Color(0xFF000000)
val primaryContainerDarkHighContrast = Color(0xFFD1B6FF)
val onPrimaryContainerDarkHighContrast = Color(0xFF120030)
val secondaryDarkHighContrast = Color(0xFFF6ECFF)
val onSecondaryDarkHighContrast = Color(0xFF000000)
val secondaryContainerDarkHighContrast = Color(0xFFD1B6FF)
val onSecondaryContainerDarkHighContrast = Color(0xFF120030)
val tertiaryDarkHighContrast = Color(0xFFFFEBF5)
val onTertiaryDarkHighContrast = Color(0xFF000000)
val tertiaryContainerDarkHighContrast = Color(0xFFFFA6E4)
val onTertiaryContainerDarkHighContrast = Color(0xFF1E0018)
val errorDarkHighContrast = Color(0xFFFFECE9)
val onErrorDarkHighContrast = Color(0xFF000000)
val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
val onErrorContainerDarkHighContrast = Color(0xFF220001)
val backgroundDarkHighContrast = Color(0xFF15111C)
val onBackgroundDarkHighContrast = Color(0xFFE8DFEF)
val surfaceDarkHighContrast = Color(0xFF15111C)
val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkHighContrast = Color(0xFF4B4456)
val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
val outlineDarkHighContrast = Color(0xFFF6ECFF)
val outlineVariantDarkHighContrast = Color(0xFFC9BED5)
val scrimDarkHighContrast = Color(0xFF000000)
val inverseSurfaceDarkHighContrast = Color(0xFFE8DFEF)
val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
val inversePrimaryDarkHighContrast = Color(0xFF5E00C6)
val surfaceDimDarkHighContrast = Color(0xFF15111C)
val surfaceBrightDarkHighContrast = Color(0xFF534E5B)
val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
val surfaceContainerLowDarkHighContrast = Color(0xFF221E29)
val surfaceContainerDarkHighContrast = Color(0xFF332E3A)
val surfaceContainerHighDarkHighContrast = Color(0xFF3E3946)
val surfaceContainerHighestDarkHighContrast = Color(0xFF4A4451)






val ColorList = listOf(
    SolarizedYellow,
    SolarizedCyan,
    SolarizedOrange,
    SolarizedBlue,
    SolarizedViolet,
    SolarizedRed,
    SolarizedMagenta,
    SolarizedGreen,
)






