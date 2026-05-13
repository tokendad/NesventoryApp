package com.tokendad.nesventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tokendad.nesventory.ui.theme.NesColors
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing

/**
 * Connection status states
 */
enum class ConnectionStatus {
    Connected,
    Disconnected,
    Connecting,
    Error,
    Unknown
}

/**
 * Status indicator dot with optional label.
 *
 * @param status Current connection status
 * @param label Optional text label
 * @param modifier Modifier
 */
@Composable
fun NesStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val color = when (status) {
        ConnectionStatus.Connected -> NesColors.success
        ConnectionStatus.Disconnected -> MaterialTheme.colorScheme.outline
        ConnectionStatus.Connecting -> NesColors.warning
        ConnectionStatus.Error -> MaterialTheme.colorScheme.error
        ConnectionStatus.Unknown -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(NesSize.statusDot)
                .clip(CircleShape)
                .background(color)
        )
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Inline status badge with background color.
 *
 * @param text Status text
 * @param status Status type for coloring
 * @param modifier Modifier
 */
@Composable
fun NesStatusBadge(
    text: String,
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ConnectionStatus.Connected -> NesColors.successContainer to NesColors.success
        ConnectionStatus.Error -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        ConnectionStatus.Connecting -> NesColors.warningContainer to NesColors.warning
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NesSpacing.xs))
            .background(backgroundColor)
            .padding(horizontal = NesSpacing.sm, vertical = NesSpacing.xs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Connection status row with label and status indicator.
 * Useful for server connection status displays.
 *
 * @param label Connection label (e.g., "Remote", "Local")
 * @param isConnected Whether connected
 * @param statusMessage Optional status message
 * @param modifier Modifier
 */
@Composable
fun NesConnectionStatus(
    label: String,
    isConnected: Boolean?,
    modifier: Modifier = Modifier,
    statusMessage: String? = null
) {
    val status = when (isConnected) {
        true -> ConnectionStatus.Connected
        false -> ConnectionStatus.Error
        null -> ConnectionStatus.Unknown
    }

    val statusText = statusMessage ?: when (status) {
        ConnectionStatus.Connected -> "Connected"
        ConnectionStatus.Error -> "Connection failed"
        ConnectionStatus.Unknown -> "Not tested"
        else -> ""
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        NesStatusIndicator(
            status = status,
            label = statusText
        )
    }
}

/**
 * Icon with status indicator overlay.
 *
 * @param icon Main icon
 * @param status Connection status
 * @param contentDescription Icon content description
 * @param modifier Modifier
 */
@Composable
fun NesIconWithStatus(
    icon: ImageVector,
    status: ConnectionStatus,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status) {
        ConnectionStatus.Connected -> NesColors.success
        ConnectionStatus.Error -> MaterialTheme.colorScheme.error
        ConnectionStatus.Connecting -> NesColors.warning
        else -> Color.Transparent
    }

    Box(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(NesSize.iconDefault)
        )
        if (status != ConnectionStatus.Unknown && status != ConnectionStatus.Disconnected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}

/**
 * Bordered container that changes color based on status.
 * Useful for input fields that show connection status.
 *
 * @param status Connection status
 * @param modifier Modifier
 * @param content Content to wrap
 */
@Composable
fun NesStatusBorder(
    status: ConnectionStatus?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val borderColor = when (status) {
        ConnectionStatus.Connected -> NesColors.success
        ConnectionStatus.Error -> MaterialTheme.colorScheme.error
        ConnectionStatus.Connecting -> NesColors.warning
        else -> Color.Transparent
    }

    val borderWidth = if (status != null && status != ConnectionStatus.Unknown) 2.dp else 0.dp

    Box(
        modifier = modifier.border(
            width = borderWidth,
            color = borderColor,
            shape = RoundedCornerShape(4.dp)
        )
    ) {
        content()
    }
}
