package com.tokendad.nesventorynew.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object RoomCategories {
    // Default categories used as fallback when API is unavailable
    val defaultCategories = listOf(
        "Living Room",
        "Bedroom",
        "Kitchen",
        "Bathroom",
        "Dining Room",
        "Office",
        "Garage",
        "Basement",
        "Attic",
        "Storage",
        "Closet",
        "Laundry Room",
        "Outdoor",
        "Other"
    )

    // Icon mappings for known categories
    val icons = mapOf(
        "Living Room" to Icons.Default.Weekend,
        "Bedroom" to Icons.Default.Bed,
        "Kitchen" to Icons.Default.Kitchen,
        "Bathroom" to Icons.Default.Bathtub,
        "Dining Room" to Icons.Default.TableRestaurant,
        "Office" to Icons.Default.Work,
        "Garage" to Icons.Default.Garage,
        "Basement" to Icons.Default.Stairs,
        "Attic" to Icons.Default.Roofing,
        "Storage" to Icons.Default.Inventory,
        "Closet" to Icons.Default.Checkroom,
        "Laundry Room" to Icons.Default.LocalLaundryService,
        "Outdoor" to Icons.Default.Park,
        "Room" to Icons.Default.MeetingRoom,
        "Primary" to Icons.Default.Home,
        "Other" to Icons.Default.Category
    )

    // Get icon for a category, with fallback for unknown categories
    fun getIcon(category: String): ImageVector {
        return icons[category] ?: Icons.Default.Category
    }
}
