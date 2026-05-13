package com.tokendad.nesventory.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing

/**
 * Empty state display for lists with no items.
 *
 * @param title Main message
 * @param message Optional secondary description
 * @param icon Optional icon (defaults to inbox)
 * @param modifier Modifier
 * @param action Optional action button
 */
@Composable
fun NesEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NesSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(NesSize.iconLarge),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(NesSpacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        message?.let {
            Spacer(modifier = Modifier.height(NesSpacing.sm))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(NesSpacing.lg))
            it()
        }
    }
}

/**
 * Loading state with centered spinner.
 *
 * @param modifier Modifier
 * @param message Optional loading message
 */
@Composable
fun NesLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NesSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        message?.let {
            Spacer(modifier = Modifier.height(NesSpacing.lg))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Full-screen loading overlay.
 *
 * @param isLoading Whether to show loading state
 * @param message Optional loading message
 * @param content Content behind the overlay
 */
@Composable
fun NesLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NesLoadingState(message = message)
            }
        }
    }
}

/**
 * Inline loading indicator (linear progress bar).
 *
 * @param isLoading Whether to show loading
 * @param modifier Modifier
 */
@Composable
fun NesInlineLoading(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        LinearProgressIndicator(
            modifier = modifier.fillMaxWidth()
        )
    }
}

/**
 * Error state display.
 *
 * @param title Error title
 * @param message Error details
 * @param modifier Modifier
 * @param icon Error icon
 * @param onRetry Optional retry action
 */
@Composable
fun NesErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Default.Warning,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NesSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(NesSize.iconLarge),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(NesSpacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        message?.let {
            Spacer(modifier = Modifier.height(NesSpacing.sm))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        onRetry?.let {
            Spacer(modifier = Modifier.height(NesSpacing.lg))
            NesSecondaryButton(
                text = "Retry",
                onClick = it,
                fullWidth = false
            )
        }
    }
}

/**
 * Message banner for inline feedback (success, error, info).
 *
 * @param message Message text
 * @param type Message type
 * @param modifier Modifier
 * @param onDismiss Optional dismiss callback
 */
@Composable
fun NesMessageBanner(
    message: String,
    type: MessageType,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val (backgroundColor, textColor) = when (type) {
        MessageType.Success -> com.tokendad.nesventory.ui.theme.NesColors.successContainer to
                com.tokendad.nesventory.ui.theme.NesColors.success
        MessageType.Error -> MaterialTheme.colorScheme.errorContainer to
                MaterialTheme.colorScheme.error
        MessageType.Warning -> com.tokendad.nesventory.ui.theme.NesColors.warningContainer to
                com.tokendad.nesventory.ui.theme.NesColors.warning
        MessageType.Info -> com.tokendad.nesventory.ui.theme.NesColors.infoContainer to
                com.tokendad.nesventory.ui.theme.NesColors.info
    }

    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.padding(NesSpacing.md)
        )
    }
}

enum class MessageType {
    Success,
    Error,
    Warning,
    Info
}
