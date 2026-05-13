package com.tokendad.nesventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Material 3 Default Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Semantic Colors - Light Theme
val SuccessLight = Color(0xFF2E7D32)      // Green 800
val SuccessContainerLight = Color(0xFFC8E6C9) // Green 100
val OnSuccessLight = Color(0xFFFFFFFF)

val WarningLight = Color(0xFFED6C02)      // Orange 700
val WarningContainerLight = Color(0xFFFFE0B2) // Orange 100
val OnWarningLight = Color(0xFFFFFFFF)

val InfoLight = Color(0xFF0288D1)         // Light Blue 700
val InfoContainerLight = Color(0xFFB3E5FC) // Light Blue 100
val OnInfoLight = Color(0xFFFFFFFF)

// Semantic Colors - Dark Theme
val SuccessDark = Color(0xFF81C784)       // Green 300
val SuccessContainerDark = Color(0xFF1B5E20) // Green 900
val OnSuccessDark = Color(0xFF000000)

val WarningDark = Color(0xFFFFB74D)       // Orange 300
val WarningContainerDark = Color(0xFFE65100) // Orange 900
val OnWarningDark = Color(0xFF000000)

val InfoDark = Color(0xFF4FC3F7)          // Light Blue 300
val InfoContainerDark = Color(0xFF01579B) // Light Blue 900
val OnInfoDark = Color(0xFF000000)

// Living item accent colors
val PersonAccent = Color(0xFF6750A4)
val PetAccent = Color(0xFF7D5260)
val PlantAccent = Color(0xFF4CAF50)

/**
 * Extended colors for NesVentory that complement Material 3.
 *
 * Usage:
 * ```
 * val colors = NesColors
 * Box(modifier = Modifier.background(colors.success))
 * ```
 */
object NesColors {
    /** Success color - use for connected states, successful operations */
    val success: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            SuccessDark else SuccessLight

    /** Success container - use for success backgrounds */
    val successContainer: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            SuccessContainerDark else SuccessContainerLight

    /** On success - text/icon color on success background */
    val onSuccess: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            OnSuccessDark else OnSuccessLight

    /** Warning color - use for warnings, attention needed */
    val warning: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            WarningDark else WarningLight

    /** Warning container - use for warning backgrounds */
    val warningContainer: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            WarningContainerDark else WarningContainerLight

    /** On warning - text/icon color on warning background */
    val onWarning: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            OnWarningDark else OnWarningLight

    /** Info color - use for informational states */
    val info: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            InfoDark else InfoLight

    /** Info container - use for info backgrounds */
    val infoContainer: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            InfoContainerDark else InfoContainerLight

    /** On info - text/icon color on info background */
    val onInfo: Color
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
            OnInfoDark else OnInfoLight
}

/**
 * Helper extension to calculate luminance for theme detection
 */
private fun Color.luminance(): Float {
    val red = this.red
    val green = this.green
    val blue = this.blue
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
