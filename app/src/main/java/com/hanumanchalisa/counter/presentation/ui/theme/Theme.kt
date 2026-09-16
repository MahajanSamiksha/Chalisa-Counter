package com.hanumanchalisa.counter.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    primaryContainer = SaffronLight,
    onPrimaryContainer = SaffronDark,
    secondary = DeepMaroon,
    onSecondary = Color.White,
    secondaryContainer = MaroonLight,
    onSecondaryContainer = DeepMaroon,
    tertiary = Turmeric,
    onTertiary = Color.White,
    tertiaryContainer = TurmericLight,
    onTertiaryContainer = TurmericDark,
    background = WarmSurfaceLight,
    onBackground = WarmOnSurfaceLight,
    surface = WarmSurfaceLight,
    onSurface = WarmOnSurfaceLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onSurfaceVariant = WarmOnSurfaceLight,
    outline = WarmOutlineLight,
)

private val DarkColors = darkColorScheme(
    primary = Turmeric,
    onPrimary = SaffronContainerDark,
    primaryContainer = SaffronContainerDark,
    onPrimaryContainer = SaffronLight,
    secondary = MaroonLight,
    onSecondary = DeepMaroon,
    secondaryContainer = DeepMaroon,
    onSecondaryContainer = MaroonLight,
    tertiary = Turmeric,
    onTertiary = TurmericDark,
    tertiaryContainer = TurmericDark,
    onTertiaryContainer = TurmericLight,
    background = WarmSurfaceDark,
    onBackground = WarmOnSurfaceDark,
    surface = WarmSurfaceDark,
    onSurface = WarmOnSurfaceDark,
    surfaceVariant = WarmSurfaceVariantDark,
    onSurfaceVariant = WarmOnSurfaceDark,
    outline = WarmOutlineDark,
)

/**
 * Applies the app's saffron theme, following the system light/dark setting.
 *
 * Dynamic (wallpaper-derived) colour is deliberately not used: the saffron identity is part of the
 * app's purpose, and a fixed palette also keeps the look consistent across Android versions.
 */
@Composable
fun HanumanChalisaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ChalisaTypography,
        content = content,
    )
}
