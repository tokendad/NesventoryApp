package com.tokendad.nesventory.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * NesVentory spacing system for consistent layout throughout the app.
 *
 * Usage:
 * ```
 * val spacing = LocalSpacing.current
 * Modifier.padding(spacing.md)
 * ```
 */
data class Spacing(
    /** Extra small spacing - 4.dp - Use for tight internal padding */
    val xs: Dp = 4.dp,

    /** Small spacing - 8.dp - Use for compact layouts, list item spacing */
    val sm: Dp = 8.dp,

    /** Medium spacing - 12.dp - Use for card internal padding */
    val md: Dp = 12.dp,

    /** Large spacing - 16.dp - Use for section padding, standard gaps */
    val lg: Dp = 16.dp,

    /** Extra large spacing - 24.dp - Use for major section breaks */
    val xl: Dp = 24.dp,

    /** Double extra large spacing - 32.dp - Use for screen-level padding */
    val xxl: Dp = 32.dp
)

/**
 * Standard component sizes for consistency
 */
object NesSize {
    /** Standard button height */
    val buttonHeight = 48.dp

    /** Compact button height */
    val buttonHeightSmall = 36.dp

    /** Standard icon size in buttons/list items */
    val iconDefault = 24.dp

    /** Small icon size */
    val iconSmall = 20.dp

    /** Large icon size for empty states */
    val iconLarge = 48.dp

    /** Thumbnail size in list items */
    val thumbnailSmall = 40.dp

    /** Medium thumbnail */
    val thumbnailMedium = 56.dp

    /** Large thumbnail/avatar */
    val thumbnailLarge = 80.dp

    /** Status indicator dot size */
    val statusDot = 8.dp

    /** Minimum touch target size (accessibility) */
    val minTouchTarget = 48.dp
}

val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Access spacing values from composition.
 *
 * Usage: `NesSpacing.md` or `LocalSpacing.current.md`
 */
object NesSpacing {
    val xs: Dp @Composable @ReadOnlyComposable get() = LocalSpacing.current.xs
    val sm: Dp @Composable @ReadOnlyComposable get() = LocalSpacing.current.sm
    val md: Dp @Composable @ReadOnlyComposable get() = LocalSpacing.current.md
    val lg: Dp @Composable @ReadOnlyComposable get() = LocalSpacing.current.lg
    val xl: Dp @Composable @ReadOnlyComposable get() = LocalSpacing.current.xl
    val xxl: Dp @Composable @ReadOnlyComposable get() = LocalSpacing.current.xxl
}
