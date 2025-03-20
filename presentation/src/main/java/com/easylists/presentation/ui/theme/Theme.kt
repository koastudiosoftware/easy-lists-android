package com.easylists.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easylists.domain.models.Themes

//region merlotLightScheme
private val merlotLightScheme = lightColorScheme(
    primary = merlot_primaryLight,
    onPrimary = merlot_onPrimaryLight,
    primaryContainer = merlot_primaryContainerLight,
    onPrimaryContainer = merlot_onPrimaryContainerLight,
    secondary = merlot_secondaryLight,
    onSecondary = merlot_onSecondaryLight,
    secondaryContainer = merlot_secondaryContainerLight,
    onSecondaryContainer = merlot_onSecondaryContainerLight,
    tertiary = merlot_tertiaryLight,
    onTertiary = merlot_onTertiaryLight,
    tertiaryContainer = merlot_tertiaryContainerLight,
    onTertiaryContainer = merlot_onTertiaryContainerLight,
    error = merlot_errorLight,
    onError = merlot_onErrorLight,
    errorContainer = merlot_errorContainerLight,
    onErrorContainer = merlot_onErrorContainerLight,
    background = merlot_backgroundLight,
    onBackground = merlot_onBackgroundLight,
    surface = merlot_surfaceLight,
    onSurface = merlot_onSurfaceLight,
    surfaceVariant = merlot_surfaceVariantLight,
    onSurfaceVariant = merlot_onSurfaceVariantLight,
    outline = merlot_outlineLight,
    outlineVariant = merlot_outlineVariantLight,
    scrim = merlot_scrimLight,
    inverseSurface = merlot_inverseSurfaceLight,
    inverseOnSurface = merlot_inverseOnSurfaceLight,
    inversePrimary = merlot_inversePrimaryLight,
    surfaceDim = merlot_surfaceDimLight,
    surfaceBright = merlot_surfaceBrightLight,
    surfaceContainerLowest = merlot_surfaceContainerLowestLight,
    surfaceContainerLow = merlot_surfaceContainerLowLight,
    surfaceContainer = merlot_surfaceContainerLight,
    surfaceContainerHigh = merlot_surfaceContainerHighLight,
    surfaceContainerHighest = merlot_surfaceContainerHighestLight,
)
//endregion


//region nauticalLightScheme
private val nauticalLightScheme = lightColorScheme(
    primary = nautical_primaryLight,
    onPrimary = nautical_onPrimaryLight,
    primaryContainer = nautical_primaryContainerLight,
    onPrimaryContainer = nautical_onPrimaryContainerLight,
    secondary = nautical_secondaryLight,
    onSecondary = nautical_onSecondaryLight,
    secondaryContainer = nautical_secondaryContainerLight,
    onSecondaryContainer = nautical_onSecondaryContainerLight,
    tertiary = nautical_tertiaryLight,
    onTertiary = nautical_onTertiaryLight,
    tertiaryContainer = nautical_tertiaryContainerLight,
    onTertiaryContainer = nautical_onTertiaryContainerLight,
    error = nautical_errorLight,
    onError = nautical_onErrorLight,
    errorContainer = nautical_errorContainerLight,
    onErrorContainer = nautical_onErrorContainerLight,
    background = nautical_backgroundLight,
    onBackground = nautical_onBackgroundLight,
    surface = nautical_surfaceLight,
    onSurface = nautical_onSurfaceLight,
    surfaceVariant = nautical_surfaceVariantLight,
    onSurfaceVariant = nautical_onSurfaceVariantLight,
    outline = nautical_outlineLight,
    outlineVariant = nautical_outlineVariantLight,
    scrim = nautical_scrimLight,
    inverseSurface = nautical_inverseSurfaceLight,
    inverseOnSurface = nautical_inverseOnSurfaceLight,
    inversePrimary = nautical_inversePrimaryLight,
    surfaceDim = nautical_surfaceDimLight,
    surfaceBright = nautical_surfaceBrightLight,
    surfaceContainerLowest = nautical_surfaceContainerLowestLight,
    surfaceContainerLow = nautical_surfaceContainerLowLight,
    surfaceContainer = nautical_surfaceContainerLight,
    surfaceContainerHigh = nautical_surfaceContainerHighLight,
    surfaceContainerHighest = nautical_surfaceContainerHighestLight,
)
//endregion


//region oceanLightScheme
private val oceanLightScheme = lightColorScheme(
    primary = ocean_primaryLight,
    onPrimary = ocean_onPrimaryLight,
    primaryContainer = ocean_primaryContainerLight,
    onPrimaryContainer = ocean_onPrimaryContainerLight,
    secondary = ocean_secondaryLight,
    onSecondary = ocean_onSecondaryLight,
    secondaryContainer = ocean_secondaryContainerLight,
    onSecondaryContainer = ocean_onSecondaryContainerLight,
    tertiary = ocean_tertiaryLight,
    onTertiary = ocean_onTertiaryLight,
    tertiaryContainer = ocean_tertiaryContainerLight,
    onTertiaryContainer = ocean_onTertiaryContainerLight,
    error = ocean_errorLight,
    onError = ocean_onErrorLight,
    errorContainer = ocean_errorContainerLight,
    onErrorContainer = ocean_onErrorContainerLight,
    background = ocean_backgroundLight,
    onBackground = ocean_onBackgroundLight,
    surface = ocean_surfaceLight,
    onSurface = ocean_onSurfaceLight,
    surfaceVariant = ocean_surfaceVariantLight,
    onSurfaceVariant = ocean_onSurfaceVariantLight,
    outline = ocean_outlineLight,
    outlineVariant = ocean_outlineVariantLight,
    scrim = ocean_scrimLight,
    inverseSurface = ocean_inverseSurfaceLight,
    inverseOnSurface = ocean_inverseOnSurfaceLight,
    inversePrimary = ocean_inversePrimaryLight,
    surfaceDim = ocean_surfaceDimLight,
    surfaceBright = ocean_surfaceBrightLight,
    surfaceContainerLowest = ocean_surfaceContainerLowestLight,
    surfaceContainerLow = ocean_surfaceContainerLowLight,
    surfaceContainer = ocean_surfaceContainerLight,
    surfaceContainerHigh = ocean_surfaceContainerHighLight,
    surfaceContainerHighest = ocean_surfaceContainerHighestLight,
)
//endregion


//region slateBlueLightScheme
private val slateBlueLightScheme = lightColorScheme(
    primary = slate_blue_primaryLight,
    onPrimary = slate_blue_onPrimaryLight,
    primaryContainer = slate_blue_primaryContainerLight,
    onPrimaryContainer = slate_blue_onPrimaryContainerLight,
    secondary = slate_blue_secondaryLight,
    onSecondary = slate_blue_onSecondaryLight,
    secondaryContainer = slate_blue_secondaryContainerLight,
    onSecondaryContainer = slate_blue_onSecondaryContainerLight,
    tertiary = slate_blue_tertiaryLight,
    onTertiary = slate_blue_onTertiaryLight,
    tertiaryContainer = slate_blue_tertiaryContainerLight,
    onTertiaryContainer = slate_blue_onTertiaryContainerLight,
    error = slate_blue_errorLight,
    onError = slate_blue_onErrorLight,
    errorContainer = slate_blue_errorContainerLight,
    onErrorContainer = slate_blue_onErrorContainerLight,
    background = slate_blue_backgroundLight,
    onBackground = slate_blue_onBackgroundLight,
    surface = slate_blue_surfaceLight,
    onSurface = slate_blue_onSurfaceLight,
    surfaceVariant = slate_blue_surfaceVariantLight,
    onSurfaceVariant = slate_blue_onSurfaceVariantLight,
    outline = slate_blue_outlineLight,
    outlineVariant = slate_blue_outlineVariantLight,
    scrim = slate_blue_scrimLight,
    inverseSurface = slate_blue_inverseSurfaceLight,
    inverseOnSurface = slate_blue_inverseOnSurfaceLight,
    inversePrimary = slate_blue_inversePrimaryLight,
    surfaceDim = slate_blue_surfaceDimLight,
    surfaceBright = slate_blue_surfaceBrightLight,
    surfaceContainerLowest = slate_blue_surfaceContainerLowestLight,
    surfaceContainerLow = slate_blue_surfaceContainerLowLight,
    surfaceContainer = slate_blue_surfaceContainerLight,
    surfaceContainerHigh = slate_blue_surfaceContainerHighLight,
    surfaceContainerHighest = slate_blue_surfaceContainerHighestLight,
)
//endregion


//region solarizedLightScheme
private val solarizedLightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)
//endregion


//region tropicalFoliageLightScheme
private val tropicalFoliageLightScheme = lightColorScheme(
    primary = tf_primaryLight,
    onPrimary = tf_onPrimaryLight,
    primaryContainer = tf_primaryContainerLight,
    onPrimaryContainer = tf_onPrimaryContainerLight,
    secondary = tf_secondaryLight,
    onSecondary = tf_onSecondaryLight,
    secondaryContainer = tf_secondaryContainerLight,
    onSecondaryContainer = tf_onSecondaryContainerLight,
    tertiary = tf_tertiaryLight,
    onTertiary = tf_onTertiaryLight,
    tertiaryContainer = tf_tertiaryContainerLight,
    onTertiaryContainer = tf_onTertiaryContainerLight,
    error = tf_errorLight,
    onError = tf_onErrorLight,
    errorContainer = tf_errorContainerLight,
    onErrorContainer = tf_onErrorContainerLight,
    background = tf_backgroundLight,
    onBackground = tf_onBackgroundLight,
    surface = tf_surfaceLight,
    onSurface = tf_onSurfaceLight,
    surfaceVariant = tf_surfaceVariantLight,
    onSurfaceVariant = tf_onSurfaceVariantLight,
    outline = tf_outlineLight,
    outlineVariant = tf_outlineVariantLight,
    scrim = tf_scrimLight,
    inverseSurface = tf_inverseSurfaceLight,
    inverseOnSurface = tf_inverseOnSurfaceLight,
    inversePrimary = tf_inversePrimaryLight,
    surfaceDim = tf_surfaceDimLight,
    surfaceBright = tf_surfaceBrightLight,
    surfaceContainerLowest = tf_surfaceContainerLowestLight,
    surfaceContainerLow = tf_surfaceContainerLowLight,
    surfaceContainer = tf_surfaceContainerLight,
    surfaceContainerHigh = tf_surfaceContainerHighLight,
    surfaceContainerHighest = tf_surfaceContainerHighestLight,
)
//endregion


//region merlotDarkScheme
private val merlotDarkScheme = darkColorScheme(
    primary = merlot_primaryDark,
    onPrimary = merlot_onPrimaryDark,
    primaryContainer = merlot_primaryContainerDark,
    onPrimaryContainer = merlot_onPrimaryContainerDark,
    secondary = merlot_secondaryDark,
    onSecondary = merlot_onSecondaryDark,
    secondaryContainer = merlot_secondaryContainerDark,
    onSecondaryContainer = merlot_onSecondaryContainerDark,
    tertiary = merlot_tertiaryDark,
    onTertiary = merlot_onTertiaryDark,
    tertiaryContainer = merlot_tertiaryContainerDark,
    onTertiaryContainer = merlot_onTertiaryContainerDark,
    error = merlot_errorDark,
    onError = merlot_onErrorDark,
    errorContainer = merlot_errorContainerDark,
    onErrorContainer = merlot_onErrorContainerDark,
    background = merlot_backgroundDark,
    onBackground = merlot_onBackgroundDark,
    surface = merlot_surfaceDark,
    onSurface = merlot_onSurfaceDark,
    surfaceVariant = merlot_surfaceVariantDark,
    onSurfaceVariant = merlot_onSurfaceVariantDark,
    outline = merlot_outlineDark,
    outlineVariant = merlot_outlineVariantDark,
    scrim = merlot_scrimDark,
    inverseSurface = merlot_inverseSurfaceDark,
    inverseOnSurface = merlot_inverseOnSurfaceDark,
    inversePrimary = merlot_inversePrimaryDark,
    surfaceDim = merlot_surfaceDimDark,
    surfaceBright = merlot_surfaceBrightDark,
    surfaceContainerLowest = merlot_surfaceContainerLowestDark,
    surfaceContainerLow = merlot_surfaceContainerLowDark,
    surfaceContainer = merlot_surfaceContainerDark,
    surfaceContainerHigh = merlot_surfaceContainerHighDark,
    surfaceContainerHighest = merlot_surfaceContainerHighestDark,
)
//endregion


//region nauticalDarkScheme
private val nauticalDarkScheme = darkColorScheme(
    primary = nautical_primaryDark,
    onPrimary = nautical_onPrimaryDark,
    primaryContainer = nautical_primaryContainerDark,
    onPrimaryContainer = nautical_onPrimaryContainerDark,
    secondary = nautical_secondaryDark,
    onSecondary = nautical_onSecondaryDark,
    secondaryContainer = nautical_secondaryContainerDark,
    onSecondaryContainer = nautical_onSecondaryContainerDark,
    tertiary = nautical_tertiaryDark,
    onTertiary = nautical_onTertiaryDark,
    tertiaryContainer = nautical_tertiaryContainerDark,
    onTertiaryContainer = nautical_onTertiaryContainerDark,
    error = nautical_errorDark,
    onError = nautical_onErrorDark,
    errorContainer = nautical_errorContainerDark,
    onErrorContainer = nautical_onErrorContainerDark,
    background = nautical_backgroundDark,
    onBackground = nautical_onBackgroundDark,
    surface = nautical_surfaceDark,
    onSurface = nautical_onSurfaceDark,
    surfaceVariant = nautical_surfaceVariantDark,
    onSurfaceVariant = nautical_onSurfaceVariantDark,
    outline = nautical_outlineDark,
    outlineVariant = nautical_outlineVariantDark,
    scrim = nautical_scrimDark,
    inverseSurface = nautical_inverseSurfaceDark,
    inverseOnSurface = nautical_inverseOnSurfaceDark,
    inversePrimary = nautical_inversePrimaryDark,
    surfaceDim = nautical_surfaceDimDark,
    surfaceBright = nautical_surfaceBrightDark,
    surfaceContainerLowest = nautical_surfaceContainerLowestDark,
    surfaceContainerLow = nautical_surfaceContainerLowDark,
    surfaceContainer = nautical_surfaceContainerDark,
    surfaceContainerHigh = nautical_surfaceContainerHighDark,
    surfaceContainerHighest = nautical_surfaceContainerHighestDark,
)
//endregion


//region oceanDarkScheme
private val oceanDarkScheme = darkColorScheme(
    primary = ocean_primaryDark,
    onPrimary = ocean_onPrimaryDark,
    primaryContainer = ocean_primaryContainerDark,
    onPrimaryContainer = ocean_onPrimaryContainerDark,
    secondary = ocean_secondaryDark,
    onSecondary = ocean_onSecondaryDark,
    secondaryContainer = ocean_secondaryContainerDark,
    onSecondaryContainer = ocean_onSecondaryContainerDark,
    tertiary = ocean_tertiaryDark,
    onTertiary = ocean_onTertiaryDark,
    tertiaryContainer = ocean_tertiaryContainerDark,
    onTertiaryContainer = ocean_onTertiaryContainerDark,
    error = ocean_errorDark,
    onError = ocean_onErrorDark,
    errorContainer = ocean_errorContainerDark,
    onErrorContainer = ocean_onErrorContainerDark,
    background = ocean_backgroundDark,
    onBackground = ocean_onBackgroundDark,
    surface = ocean_surfaceDark,
    onSurface = ocean_onSurfaceDark,
    surfaceVariant = ocean_surfaceVariantDark,
    onSurfaceVariant = ocean_onSurfaceVariantDark,
    outline = ocean_outlineDark,
    outlineVariant = ocean_outlineVariantDark,
    scrim = ocean_scrimDark,
    inverseSurface = ocean_inverseSurfaceDark,
    inverseOnSurface = ocean_inverseOnSurfaceDark,
    inversePrimary = ocean_inversePrimaryDark,
    surfaceDim = ocean_surfaceDimDark,
    surfaceBright = ocean_surfaceBrightDark,
    surfaceContainerLowest = ocean_surfaceContainerLowestDark,
    surfaceContainerLow = ocean_surfaceContainerLowDark,
    surfaceContainer = ocean_surfaceContainerDark,
    surfaceContainerHigh = ocean_surfaceContainerHighDark,
    surfaceContainerHighest = ocean_surfaceContainerHighestDark,
)
//endregion


//region slateBlueDarkScheme
private val slateBlueDarkScheme = darkColorScheme(
    primary = slate_blue_primaryDark,
    onPrimary = slate_blue_onPrimaryDark,
    primaryContainer = slate_blue_primaryContainerDark,
    onPrimaryContainer = slate_blue_onPrimaryContainerDark,
    secondary = slate_blue_secondaryDark,
    onSecondary = slate_blue_onSecondaryDark,
    secondaryContainer = slate_blue_secondaryContainerDark,
    onSecondaryContainer = slate_blue_onSecondaryContainerDark,
    tertiary = slate_blue_tertiaryDark,
    onTertiary = slate_blue_onTertiaryDark,
    tertiaryContainer = slate_blue_tertiaryContainerDark,
    onTertiaryContainer = slate_blue_onTertiaryContainerDark,
    error = slate_blue_errorDark,
    onError = slate_blue_onErrorDark,
    errorContainer = slate_blue_errorContainerDark,
    onErrorContainer = slate_blue_onErrorContainerDark,
    background = slate_blue_backgroundDark,
    onBackground = slate_blue_onBackgroundDark,
    surface = slate_blue_surfaceDark,
    onSurface = slate_blue_onSurfaceDark,
    surfaceVariant = slate_blue_surfaceVariantDark,
    onSurfaceVariant = slate_blue_onSurfaceVariantDark,
    outline = slate_blue_outlineDark,
    outlineVariant = slate_blue_outlineVariantDark,
    scrim = slate_blue_scrimDark,
    inverseSurface = slate_blue_inverseSurfaceDark,
    inverseOnSurface = slate_blue_inverseOnSurfaceDark,
    inversePrimary = slate_blue_inversePrimaryDark,
    surfaceDim = slate_blue_surfaceDimDark,
    surfaceBright = slate_blue_surfaceBrightDark,
    surfaceContainerLowest = slate_blue_surfaceContainerLowestDark,
    surfaceContainerLow = slate_blue_surfaceContainerLowDark,
    surfaceContainer = slate_blue_surfaceContainerDark,
    surfaceContainerHigh = slate_blue_surfaceContainerHighDark,
    surfaceContainerHighest = slate_blue_surfaceContainerHighestDark,
)
//endregion


//region solarizedDarkScheme
private val solarizedDarkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)
//endregion


//region tropicalFoliageDarkScheme
private val tropicalFoliageDarkScheme = darkColorScheme(
    primary = tf_primaryDark,
    onPrimary = tf_onPrimaryDark,
    primaryContainer = tf_primaryContainerDark,
    onPrimaryContainer = tf_onPrimaryContainerDark,
    secondary = tf_secondaryDark,
    onSecondary = tf_onSecondaryDark,
    secondaryContainer = tf_secondaryContainerDark,
    onSecondaryContainer = tf_onSecondaryContainerDark,
    tertiary = tf_tertiaryDark,
    onTertiary = tf_onTertiaryDark,
    tertiaryContainer = tf_tertiaryContainerDark,
    onTertiaryContainer = tf_onTertiaryContainerDark,
    error = tf_errorDark,
    onError = tf_onErrorDark,
    errorContainer = tf_errorContainerDark,
    onErrorContainer = tf_onErrorContainerDark,
    background = tf_backgroundDark,
    onBackground = tf_onBackgroundDark,
    surface = tf_surfaceDark,
    onSurface = tf_onSurfaceDark,
    surfaceVariant = tf_surfaceVariantDark,
    onSurfaceVariant = tf_onSurfaceVariantDark,
    outline = tf_outlineDark,
    outlineVariant = tf_outlineVariantDark,
    scrim = tf_scrimDark,
    inverseSurface = tf_inverseSurfaceDark,
    inverseOnSurface = tf_inverseOnSurfaceDark,
    inversePrimary = tf_inversePrimaryDark,
    surfaceDim = tf_surfaceDimDark,
    surfaceBright = tf_surfaceBrightDark,
    surfaceContainerLowest = tf_surfaceContainerLowestDark,
    surfaceContainerLow = tf_surfaceContainerLowDark,
    surfaceContainer = tf_surfaceContainerDark,
    surfaceContainerHigh = tf_surfaceContainerHighDark,
    surfaceContainerHighest = tf_surfaceContainerHighestDark,
)
//endregion


/** CompositionLocal key to provide and consume the applied theme mode down the composition. */
val LocalThemeMode = staticCompositionLocalOf { Themes.Default }

@Composable
fun EasyListsTheme(
    themeMode: Themes = Themes.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    CompositionLocalProvider(
        LocalThemeMode provides themeMode
    ) {
        val colorScheme = when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                when (themeMode) {
                    Themes.Merlot -> merlotDarkScheme
                    Themes.Nautical -> nauticalDarkScheme
                    Themes.Ocean -> oceanDarkScheme
                    Themes.SlateBlue -> slateBlueDarkScheme
                    Themes.TropicalFoliage -> tropicalFoliageDarkScheme
                    else -> solarizedDarkScheme
                }
            }

            else -> {
                when (themeMode) {
                    Themes.Merlot -> merlotLightScheme
                    Themes.Nautical -> nauticalLightScheme
                    Themes.Ocean -> oceanLightScheme
                    Themes.SlateBlue -> slateBlueLightScheme
                    Themes.TropicalFoliage -> tropicalFoliageLightScheme
                    else -> solarizedLightScheme
                }
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}


//region Easy List custom spacing values
data class CustomSpaces(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val mediumLarge: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 24.dp,

    val rowHeightMedium: Dp = 54.dp,
    val rowHeightLarge: Dp = 72.dp,
)


val MaterialTheme.spaces: CustomSpaces
    @Composable
    get() = CustomSpaces()
//endregion
