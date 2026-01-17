# API Sync Implementation Plan

This document outlines the changes needed to sync the Android app with recent backend API updates.

## Overview

Three updates are required based on recent backend commits:
1. **Dynamic Location Categories** - Fetch categories from API instead of hardcoded list
2. **Printer Test Connection** - Add new endpoint for testing printer connectivity
3. **Field Name Update** - Rename `room_category` to `location_category` to match backend

---

## 1. Dynamic Location Categories

### Current State
- Hardcoded categories in `util/RoomCategories.kt`
- 14 predefined categories with icon mappings
- Used in AddLocationScreen, EditLocationScreen, LocationDetailScreen

### New Backend Endpoint
```
GET /api/settings/location-categories

Response: ["Primary", "Room", "Garage", "Attic", "Dungeon", ...]
```

### Implementation Plan

#### Step 1.1: Add API Endpoint
**File:** `data/remote/NesVentoryApi.kt`
```kotlin
@GET("api/settings/location-categories")
suspend fun getLocationCategories(): List<String>
```

#### Step 1.2: Update RoomCategories Utility
**File:** `util/RoomCategories.kt`

Convert from static object to a class that can hold dynamic categories while preserving icon mappings for known categories:

```kotlin
object RoomCategories {
    // Default categories (used as fallback)
    val defaultCategories = listOf(
        "Living Room", "Bedroom", "Kitchen", "Bathroom",
        "Dining Room", "Office", "Garage", "Basement",
        "Attic", "Storage", "Closet", "Laundry Room",
        "Outdoor", "Other"
    )

    // Icon mappings for known categories
    val icons = mapOf(
        "Living Room" to Icons.Default.Weekend,
        "Bedroom" to Icons.Default.Bed,
        // ... existing mappings ...
        // Fallback handled in getIcon() function
    )

    fun getIcon(category: String): ImageVector {
        return icons[category] ?: Icons.Default.Category
    }
}
```

#### Step 1.3: Add State to ViewModels
**Files:**
- `ui/addlocation/AddLocationViewModel.kt`
- `ui/editlocation/EditLocationViewModel.kt`

Add:
```kotlin
var locationCategories by mutableStateOf<List<String>>(RoomCategories.defaultCategories)

init {
    fetchLocationCategories()
}

private fun fetchLocationCategories() {
    viewModelScope.launch {
        try {
            locationCategories = api.getLocationCategories()
        } catch (_: Exception) {
            // Fall back to defaults silently
        }
    }
}
```

#### Step 1.4: Update Screens to Use Dynamic Categories
**Files:**
- `ui/addlocation/AddLocationScreen.kt`
- `ui/editlocation/EditLocationScreen.kt` (if applicable)

Change from:
```kotlin
RoomCategories.categories.forEach { category ->
```
To:
```kotlin
viewModel.locationCategories.forEach { category ->
```

---

## 2. Printer Test Connection Endpoint

### New Backend Endpoint
```
POST /api/printer/test-connection

Request:
{
  "enabled": true,
  "model": "d11_h",
  "connection_type": "server",
  "address": "AA:BB:CC:DD:EE:FF",
  "density": 3
}

Response:
{
  "success": true,
  "message": "Successfully connected to printer"
}
```

### Implementation Plan

#### Step 2.1: Add Response Model
**File:** `data/remote/PrinterModels.kt`
```kotlin
data class PrinterTestResult(
    val success: Boolean,
    val message: String
)
```

#### Step 2.2: Add API Endpoint
**File:** `data/remote/NesVentoryApi.kt`
```kotlin
@POST("api/printer/test-connection")
suspend fun testPrinterConnection(@Body config: PrinterConfig): PrinterTestResult
```

---

## 3. Field Name Update: room_category → location_category

### Current State
- `Location` model uses `room_category`
- `LocationCreate` model uses `room_category`
- Multiple ViewModels reference `room_category`
- Screens display based on `room_category`

### Backend Expectation
- Field is now named `location_category`

### Implementation Plan

#### Step 3.1: Update Location Models
**File:** `data/remote/LocationModels.kt`

Change:
```kotlin
val room_category: String? = null
```
To:
```kotlin
val location_category: String? = null
```

In both `Location` and `LocationCreate` data classes.

#### Step 3.2: Update ViewModels
**Files:**
- `ui/addlocation/AddLocationViewModel.kt`
- `ui/editlocation/EditLocationViewModel.kt`

Update field references from `room_category` to `location_category`.

#### Step 3.3: Update Screens
**Files:**
- `ui/locationdetail/LocationDetailScreen.kt`
- `ui/addlocation/AddLocationScreen.kt`

Update references from `location.room_category` to `location.location_category`.

---

## Files to Modify

| File | Changes |
|------|---------|
| `data/remote/NesVentoryApi.kt` | Add `getLocationCategories()` and `testPrinterConnection()` |
| `data/remote/LocationModels.kt` | Rename `room_category` → `location_category` |
| `data/remote/PrinterModels.kt` | Add `PrinterTestResult` model |
| `util/RoomCategories.kt` | Add `getIcon()` helper, keep as fallback |
| `ui/addlocation/AddLocationViewModel.kt` | Fetch categories, update field name |
| `ui/addlocation/AddLocationScreen.kt` | Use dynamic categories, update field name |
| `ui/editlocation/EditLocationViewModel.kt` | Fetch categories, update field name |
| `ui/editlocation/EditLocationScreen.kt` | Use dynamic categories (if applicable) |
| `ui/locationdetail/LocationDetailScreen.kt` | Update field name reference |

---

## Testing Checklist

- [x] App compiles successfully
- [ ] Location categories load from API
- [ ] Fallback to default categories works when API fails
- [ ] Icons display correctly for known and unknown categories
- [ ] Add Location screen shows dynamic categories
- [ ] Edit Location screen shows dynamic categories
- [ ] Location Detail screen displays category correctly
- [ ] Printer test connection endpoint is callable

## Implementation Status

**Completed: 2026-01-16**

All changes have been implemented and the build compiles successfully.

---

## Rollback Plan

If issues arise, the changes can be reverted by:
1. Restoring `room_category` field name in models
2. Reverting to static `RoomCategories.categories` list
3. Removing the new API endpoints (they're additive, won't break existing functionality)
