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
                    Themes.Ocean -> oceanDarkScheme
                    Themes.TropicalFoliage -> tropicalFoliageDarkScheme
                    else -> solarizedDarkScheme
                }
            }

            else -> {
                when (themeMode) {
                    Themes.Ocean -> oceanLightScheme
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
)


val MaterialTheme.spaces: CustomSpaces
    @Composable
    get() = CustomSpaces()
//endregion
