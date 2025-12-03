package com.nesventory.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * NesVentory dark color scheme - matches base web app dark theme
 */
private val DarkColorScheme = darkColorScheme(
    primary = NesVentoryPrimary,
    onPrimary = NesVentoryOnPrimary,
    primaryContainer = NesVentoryPrimaryContainer,
    onPrimaryContainer = NesVentoryOnSurface,
    secondary = NesVentorySecondary,
    onSecondary = NesVentoryOnSecondary,
    secondaryContainer = NesVentorySecondaryContainer,
    onSecondaryContainer = NesVentoryOnSurface,
    tertiary = NesVentoryTertiary,
    onTertiary = NesVentoryOnPrimary,
    tertiaryContainer = NesVentoryTertiaryContainer,
    onTertiaryContainer = NesVentoryOnSurface,
    error = NesVentoryError,
    onError = NesVentoryOnPrimary,
    errorContainer = NesVentoryErrorContainer,
    onErrorContainer = NesVentoryOnSurface,
    background = NesVentoryBackground,
    onBackground = NesVentoryOnBackground,
    surface = NesVentoryBackgroundElevated,
    onSurface = NesVentoryOnSurface,
    surfaceVariant = NesVentorySurfaceVariant,
    onSurfaceVariant = NesVentoryOnSurfaceVariant,
    outline = NesVentoryOutline,
    outlineVariant = NesVentoryOutlineVariant,
    inverseSurface = NesVentorySurfaceLight,
    inverseOnSurface = NesVentoryOnSurfaceLight,
    inversePrimary = NesVentoryPrimaryLight
)

/**
 * NesVentory light color scheme - matches base web app light theme
 */
private val LightColorScheme = lightColorScheme(
    primary = NesVentoryPrimaryLight,
    onPrimary = NesVentoryOnPrimaryLight,
    primaryContainer = NesVentoryPrimaryContainerLight,
    onPrimaryContainer = NesVentoryOnSurfaceLight,
    secondary = NesVentorySecondaryLight,
    onSecondary = NesVentoryOnSecondaryLight,
    secondaryContainer = NesVentorySecondaryContainerLight,
    onSecondaryContainer = NesVentoryOnSurfaceLight,
    tertiary = NesVentoryTertiaryLight,
    onTertiary = NesVentoryOnPrimaryLight,
    tertiaryContainer = NesVentoryTertiaryContainerLight,
    onTertiaryContainer = NesVentoryOnSurfaceLight,
    error = NesVentoryErrorLight,
    onError = NesVentoryOnPrimaryLight,
    errorContainer = NesVentoryErrorContainerLight,
    onErrorContainer = NesVentoryOnSurfaceLight,
    background = NesVentoryBackgroundLight,
    onBackground = NesVentoryOnBackgroundLight,
    surface = NesVentorySurfaceLight,
    onSurface = NesVentoryOnSurfaceLight,
    surfaceVariant = NesVentorySurfaceVariantLight,
    onSurfaceVariant = NesVentoryOnSurfaceVariantLight,
    outline = NesVentoryOutlineLight,
    outlineVariant = NesVentoryOutlineVariantLight,
    inverseSurface = NesVentoryBackgroundElevated,
    inverseOnSurface = NesVentoryOnSurface,
    inversePrimary = NesVentoryPrimary
)

/**
 * NesVentory theme composable - follows system dark/light preference
 * Dynamic colors are disabled to maintain brand consistency with the web app
 */
@Composable
fun NesVentoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
