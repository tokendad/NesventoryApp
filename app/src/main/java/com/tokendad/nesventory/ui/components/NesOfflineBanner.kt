package com.tokendad.nesventory.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tokendad.nesventory.ui.theme.NesSpacing

@Composable
fun NesOfflineBanner(
    modifier: Modifier = Modifier,
    message: String = "Offline mode: showing cached data"
) {
    NesSectionCard(
        title = "Offline",
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = NesSpacing.xs)
        )
    }
}
