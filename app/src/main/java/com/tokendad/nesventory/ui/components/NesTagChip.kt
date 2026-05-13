package com.tokendad.nesventory.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tokendad.nesventory.data.remote.Tag
import com.tokendad.nesventory.util.ColorUtils

@Composable
fun NesTagChip(
    tag: Tag,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val chipColor = ColorUtils.parseHexColor(tag.color)

    InputChip(
        selected = false,
        onClick = { onDelete?.invoke() },
        label = { Text(tag.name) },
        trailingIcon = if (onDelete != null) {
            {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove ${tag.name}"
                )
            }
        } else {
            null
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = chipColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    )
}
