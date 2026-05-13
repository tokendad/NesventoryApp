package com.tokendad.nesventory.util

import androidx.compose.ui.graphics.Color

object ColorUtils {
    fun parseHexColor(hex: String?): Color? {
        if (hex.isNullOrBlank()) return null
        val normalized = if (hex.startsWith("#")) hex else "#$hex"
        return try {
            Color(android.graphics.Color.parseColor(normalized))
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
