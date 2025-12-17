package com.nesventory.android.util

/**
 * Extension function to format enum names for display.
 * Converts SCREAMING_SNAKE_CASE to lowercase with spaces.
 * Example: MULTI_FAMILY -> "multi family"
 */
fun Enum<*>.toDisplayName(): String {
    return this.name.lowercase().replace('_', ' ')
}
