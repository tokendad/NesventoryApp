package com.tokendad.nesventory.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing

/**
 * Primary filled button with consistent height and optional loading state.
 *
 * @param text Button label
 * @param onClick Click handler
 * @param modifier Modifier
 * @param enabled Whether button is enabled
 * @param loading Show loading spinner instead of text
 * @param icon Optional leading icon
 * @param fullWidth Whether to fill available width (default true)
 */
@Composable
fun NesPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(NesSize.buttonHeight),
        enabled = enabled && !loading,
        contentPadding = PaddingValues(horizontal = NesSpacing.lg)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(NesSize.iconSmall)
                )
                Spacer(Modifier.width(NesSpacing.sm))
            }
            Text(text)
        }
    }
}

/**
 * Secondary outlined button with consistent height and optional loading state.
 *
 * @param text Button label
 * @param onClick Click handler
 * @param modifier Modifier
 * @param enabled Whether button is enabled
 * @param loading Show loading spinner instead of text
 * @param icon Optional leading icon
 * @param fullWidth Whether to fill available width (default true)
 */
@Composable
fun NesSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(NesSize.buttonHeight),
        enabled = enabled && !loading,
        contentPadding = PaddingValues(horizontal = NesSpacing.lg)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        } else {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(NesSize.iconSmall)
                )
                Spacer(Modifier.width(NesSpacing.sm))
            }
            Text(text)
        }
    }
}

/**
 * Compact button for inline actions or secondary actions.
 *
 * @param text Button label
 * @param onClick Click handler
 * @param modifier Modifier
 * @param enabled Whether button is enabled
 * @param icon Optional leading icon
 */
@Composable
fun NesCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(NesSize.buttonHeightSmall),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = NesSpacing.md)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(NesSpacing.xs))
        }
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Text button for tertiary/link-style actions.
 *
 * @param text Button label
 * @param onClick Click handler
 * @param modifier Modifier
 * @param enabled Whether button is enabled
 * @param icon Optional leading icon
 */
@Composable
fun NesTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(NesSize.iconSmall)
            )
            Spacer(Modifier.width(NesSpacing.xs))
        }
        Text(text)
    }
}

/**
 * Destructive action button (red/error colored).
 *
 * @param text Button label
 * @param onClick Click handler
 * @param modifier Modifier
 * @param enabled Whether button is enabled
 * @param loading Show loading spinner
 * @param fullWidth Whether to fill available width
 */
@Composable
fun NesDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(NesSize.buttonHeight),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        contentPadding = PaddingValues(horizontal = NesSpacing.lg)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onError,
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}
