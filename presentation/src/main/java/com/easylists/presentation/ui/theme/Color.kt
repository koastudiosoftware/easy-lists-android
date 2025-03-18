package com.easylists.presentation.ui.theme

import androidx.compose.ui.graphics.Color

//region Solarized color palette
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
//endregion

//region Tropical Foliage color palette
val TropicalFoliageBeige = Color(0xFFFAF3E3)
val TropicalFoliageSpanishPink = Color(0XFFF8D2C5)
val TropicalFoliageSeaPink = Color(0xFFF7B2AB)
val TropicalFoliageLightCoral = Color(0xFFED7E77)
val TropicalFoliageRed = Color(0xFFE74958)
val TropicalFoliageGreen3 = Color(0xFFC7C8A0)
val TropicalFoliageGreen2 = Color(0xFF879571)
val TropicalFoliageGreen1 = Color(0xFF6f8266)
val TropicalFoliageGreen0 = Color(0xFF5A735D)
val TropicalFoliageGreen00 = Color(0xFF446354)
val TropicalFoliageGreen01 = Color(0XFF32534a)
val TropicalFoliageGreen03 = Color(0xFF002323)
//endregion


//region Solarized Light
val primaryLight = SolarizedViolet                      // bottom nav background, focused OutlinedTextBox border
val onPrimaryLight = SolarizedBase3                     // text on cards
val primaryContainerLight = SolarizedViolet
val onPrimaryContainerLight = SolarizedBase3
val secondaryLight = Color(0xFF6E4AAD)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = SolarizedBase3            // selected nav button background
val onSecondaryContainerLight = SolarizedBase01
val tertiaryLight = Color(0xFF960082)
val onTertiaryLight = SolarizedBase00
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
val outlineVariantLight = SolarizedBase1_20             // horizontal divider
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF332E3A)
val inverseOnSurfaceLight = SolarizedBase00
val inversePrimaryLight = Color(0xFFD4BBFF)
val surfaceDimLight = Color(0xFFE0D7E7)
val surfaceBrightLight = Color(0xFFFEF7FF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = SolarizedBase3           // bottom sheet background color
val surfaceContainerLight = SolarizedBase2              // overflow menu background color
val surfaceContainerHighLight = SolarizedBase2
val surfaceContainerHighestLight = SolarizedBase3       // card background color
//endregion

//region Solarized Dark
val primaryDark = SolarizedCyan
val onPrimaryDark = SolarizedBase2
val primaryContainerDark = SolarizedCyan
val onPrimaryContainerDark = SolarizedBase2
val secondaryDark = Color(0xFFD4BBFF)
val onSecondaryDark = Color(0xFF3E147C)
val secondaryContainerDark = SolarizedBase03
val onSecondaryContainerDark = SolarizedBase1
val tertiaryDark = Color(0xFFFFADE5)
val onTertiaryDark = SolarizedBase0
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
val outlineVariantDark = SolarizedBase01_60
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE8DFEF)
val inverseOnSurfaceDark = SolarizedBase0
val inversePrimaryDark = Color(0xFF7917F5)
val surfaceDimDark = Color(0xFF15111C)
val surfaceBrightDark = Color(0xFF3C3743)
val surfaceContainerLowestDark = Color(0xFF100C17)
val surfaceContainerLowDark = SolarizedBase03
val surfaceContainerDark = SolarizedBase02
val surfaceContainerHighDark = SolarizedBase02
val surfaceContainerHighestDark = SolarizedBase03
//endregion

//region Tropical Foliage Light
val tf_primaryLight = TropicalFoliageGreen01
val tf_onPrimaryLight = Color(0xFFFFFFFF)
val tf_primaryContainerLight = TropicalFoliageGreen1
val tf_onPrimaryContainerLight = TropicalFoliageBeige
val tf_secondaryLight = Color(0xFF6B5E24)
val tf_onSecondaryLight = Color(0xFFFFFFFF)
val tf_secondaryContainerLight = Color(0xFFF2DF98)
val tf_onSecondaryContainerLight = TropicalFoliageGreen1
val tf_tertiaryLight = Color(0xFF4D6700)
val tf_onTertiaryLight = Color(0xFFFFFFFF)
val tf_tertiaryContainerLight = TropicalFoliageGreen2
val tf_onTertiaryContainerLight = TropicalFoliageBeige
val tf_errorLight = TropicalFoliageRed
val tf_onErrorLight = Color(0xFFFFFFFF)
val tf_errorContainerLight = TropicalFoliageRed
val tf_onErrorContainerLight = Color(0xFF93000A)
val tf_backgroundLight = TropicalFoliageBeige
val tf_onBackgroundLight = TropicalFoliageGreen01
val tf_surfaceLight = TropicalFoliageGreen3
val tf_onSurfaceLight = TropicalFoliageGreen00
val tf_surfaceVariantLight = Color(0xFFEBE2C8)
val tf_onSurfaceVariantLight = TropicalFoliageGreen00
val tf_outlineLight = Color(0xFF7D7761)
val tf_outlineVariantLight = TropicalFoliageGreen3
val tf_scrimLight = Color(0xFF000000)
val tf_inverseSurfaceLight = Color(0xFF343025)
val tf_inverseOnSurfaceLight = Color(0xFFF8F0DF)
val tf_inversePrimaryLight = Color(0xFFE5C524)
val tf_surfaceDimLight = Color(0xFFE0D9C9)
val tf_surfaceBrightLight = Color(0xFFFFF9EE)
val tf_surfaceContainerLowestLight = Color(0xFFFFFFFF)
val tf_surfaceContainerLowLight = TropicalFoliageBeige
val tf_surfaceContainerLight = TropicalFoliageGreen3
val tf_surfaceContainerHighLight = Color(0xFFEFE8D7)
val tf_surfaceContainerHighestLight = TropicalFoliageBeige

//region Tropical Foliage Dark
val tf_primaryDark = TropicalFoliageGreen3
val tf_onPrimaryDark = Color(0xFF3A3000)
val tf_primaryContainerDark = TropicalFoliageGreen1
val tf_onPrimaryContainerDark = TropicalFoliageBeige
val tf_secondaryDark = Color(0xFFD8C682)
val tf_onSecondaryDark = Color(0xFF3A3000)
val tf_secondaryContainerDark = Color(0xFF544910)
val tf_onSecondaryContainerDark = TropicalFoliageGreen0
val tf_tertiaryDark = Color(0xFFF8FFDF)
val tf_onTertiaryDark = Color(0xFF263500)
val tf_tertiaryContainerDark = TropicalFoliageGreen2
val tf_onTertiaryContainerDark = TropicalFoliageBeige
val tf_errorDark = TropicalFoliageRed
val tf_onErrorDark = Color(0xFF690005)
val tf_errorContainerDark = TropicalFoliageRed
val tf_onErrorContainerDark = Color(0xFFFFDAD6)
val tf_backgroundDark = TropicalFoliageGreen03
val tf_onBackgroundDark = TropicalFoliageGreen2
val tf_surfaceDark = TropicalFoliageGreen3
val tf_onSurfaceDark = TropicalFoliageGreen00
val tf_surfaceVariantDark = Color(0xFF4C4733)
val tf_onSurfaceVariantDark = TropicalFoliageGreen00
val tf_outlineDark = Color(0xFF989079)
val tf_outlineVariantDark = TropicalFoliageGreen00
val tf_scrimDark = Color(0xFF000000)
val tf_inverseSurfaceDark = Color(0xFFE9E2D1)
val tf_inverseOnSurfaceDark = Color(0xFF343025)
val tf_inversePrimaryDark = Color(0xFF6E5D00)
val tf_surfaceDimDark = Color(0xFF16130A)
val tf_surfaceBrightDark = Color(0xFF3D392E)
val tf_surfaceContainerLowestDark = Color(0xFF100E06)
val tf_surfaceContainerLowDark = TropicalFoliageGreen03
val tf_surfaceContainerDark = TropicalFoliageGreen3
val tf_surfaceContainerHighDark = Color(0xFF2D2A1F)
val tf_surfaceContainerHighestDark = TropicalFoliageGreen03
//endregion

