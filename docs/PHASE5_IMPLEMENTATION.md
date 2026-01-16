# Phase 5: UI/UX & Internationalization - Implementation Plan

This document outlines the Phase 5 implementation for the NesventoryNew Android application, focusing on UI polish, internationalization, and enhanced location management.

## Overview

Phase 5 adds:
1. **International Formats** - Currency and date/time localization
2. **Custom Fields** - Dynamic field rendering for items
3. **Location Room Categories** - Room/area categorization for locations

---

## Feature 1: International Formats

### 1.1 Currency Formatting

**Goal:** Display monetary values using the device's or server's locale settings.

#### Files to Modify
- `ui/itemdetail/ItemDetailScreen.kt`
- `ui/items/ItemsScreen.kt`
- `ui/locationdetail/LocationDetailScreen.kt`
- Create `util/CurrencyFormatter.kt`

#### Implementation

**Create CurrencyFormatter utility:**
```kotlin
// util/CurrencyFormatter.kt
object CurrencyFormatter {
    private val currencyFormat: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale.getDefault())
    }

    fun format(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return try {
            val amount = value.toDoubleOrNull() ?: return "$$value"
            currencyFormat.format(amount)
        } catch (e: Exception) {
            "$$value"
        }
    }

    fun format(value: Double?): String {
        if (value == null) return ""
        return currencyFormat.format(value)
    }
}
```

**Update screens to use formatter:**
- Replace hardcoded `"$$it"` with `CurrencyFormatter.format(it)`
- Apply to: estimated_value, purchase_price, estimated_property_value, purchase_price (insurance)

#### Affected Fields
| Screen | Field |
|--------|-------|
| ItemDetailScreen | estimated_value, purchase_price |
| LocationDetailScreen | estimated_property_value, estimated_value_with_items, insurance purchase_price |
| ItemsScreen | Item list values (if displayed) |

### 1.2 Date/Time Formatting

**Goal:** Display dates using locale-aware formatting.

#### Files to Modify
- `ui/itemdetail/ItemDetailScreen.kt`
- `ui/locationdetail/LocationDetailScreen.kt`
- `ui/maintenance/MaintenanceScreen.kt`
- Create `util/DateFormatter.kt`

#### Implementation

**Create DateFormatter utility:**
```kotlin
// util/DateFormatter.kt
object DateFormatter {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val outputDateFormat: DateFormat by lazy {
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
    }

    private val outputDateTimeFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
    }

    fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = inputFormat.parse(isoDate) ?: dateOnlyFormat.parse(isoDate)
            date?.let { outputDateFormat.format(it) } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatDateTime(isoDateTime: String?): String {
        if (isoDateTime.isNullOrBlank()) return ""
        return try {
            val date = inputFormat.parse(isoDateTime)
            date?.let { outputDateTimeFormat.format(it) } ?: isoDateTime
        } catch (e: Exception) {
            isoDateTime
        }
    }
}
```

**Update screens to use formatter:**
- Replace raw date strings with `DateFormatter.formatDate()` or `DateFormatter.formatDateTime()`
- Apply to: created_at, updated_at, due_date, purchase_date, completed_date

#### Affected Fields
| Screen | Field | Format Type |
|--------|-------|-------------|
| ItemDetailScreen | created_at, updated_at, purchase_date | DateTime/Date |
| LocationDetailScreen | created_at, updated_at, insurance dates | DateTime/Date |
| MaintenanceScreen | due_date, completed_date | Date |

---

## Feature 2: Custom Fields Support

### 2.1 Overview

**Goal:** Dynamically render custom fields that may be returned in the Item API response.

#### Files to Modify
- `data/remote/ItemModels.kt` - Add custom_fields to Item model
- `ui/itemdetail/ItemDetailScreen.kt` - Render custom fields section
- `ui/edititem/EditItemScreen.kt` - Edit custom fields (optional)

#### Data Model Update

```kotlin
// ItemModels.kt - Update Item data class
data class Item(
    val id: UUID,
    val name: String,
    // ... existing fields ...
    val custom_fields: Map<String, Any>? = null  // NEW
)
```

#### Implementation

**CustomFieldsSection composable:**
```kotlin
@Composable
fun CustomFieldsSection(customFields: Map<String, Any>?) {
    if (customFields.isNullOrEmpty()) return

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Custom Fields",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            customFields.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key.replace("_", " ").capitalize(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
```

**Add to ItemDetailScreen DetailsTab:**
```kotlin
// After existing fields, before timestamps
item.custom_fields?.let { fields ->
    if (fields.isNotEmpty()) {
        HorizontalDivider()
        CustomFieldsSection(fields)
    }
}
```

---

## Feature 3: Location Room Categories

### 3.1 Overview

**Goal:** Add room/area categorization to locations (e.g., Kitchen, Bedroom, Garage, Office).

#### Files to Modify
- `data/remote/LocationModels.kt` - Add room_category field
- `ui/locationdetail/LocationDetailScreen.kt` - Display room category
- `ui/addlocation/AddLocationScreen.kt` - Room category selector
- `ui/editlocation/EditLocationScreen.kt` - Room category selector

### 3.2 Data Model Update

```kotlin
// LocationModels.kt - Update Location data class
data class Location(
    val id: UUID,
    val name: String,
    // ... existing fields ...
    val room_category: String? = null  // NEW: Kitchen, Bedroom, Garage, etc.
)

data class LocationCreate(
    val name: String,
    // ... existing fields ...
    val room_category: String? = null  // NEW
)
```

### 3.3 Room Categories

**Predefined categories:**
```kotlin
object RoomCategories {
    val categories = listOf(
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
        "Other" to Icons.Default.Category
    )
}
```

### 3.4 UI Implementation

**Display in LocationDetailScreen:**
```kotlin
// In DetailsTab, after name/friendly_name
location.room_category?.let { category ->
    AssistChip(
        onClick = {},
        label = { Text(category) },
        leadingIcon = {
            Icon(
                RoomCategories.icons[category] ?: Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}
```

**Selector in AddLocationScreen/EditLocationScreen:**
```kotlin
@Composable
fun RoomCategorySelector(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected ?: "Select Category",
            onValueChange = {},
            readOnly = true,
            label = { Text("Room Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            RoomCategories.categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    leadingIcon = {
                        Icon(
                            RoomCategories.icons[category] ?: Icons.Default.Category,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
```

---

## Implementation Order

1. **Utility Classes** (Foundation)
   - Create `util/CurrencyFormatter.kt`
   - Create `util/DateFormatter.kt`
   - Create `util/RoomCategories.kt`

2. **Data Models** (Backend alignment)
   - Add `custom_fields` to Item model
   - Add `room_category` to Location/LocationCreate models

3. **Currency Formatting** (UI updates)
   - Update ItemDetailScreen
   - Update LocationDetailScreen
   - Update any list screens showing values

4. **Date Formatting** (UI updates)
   - Update all screens showing dates
   - Update MaintenanceScreen

5. **Custom Fields** (New feature)
   - Add CustomFieldsSection composable
   - Integrate into ItemDetailScreen

6. **Room Categories** (New feature)
   - Add RoomCategorySelector composable
   - Update LocationDetailScreen
   - Update AddLocationScreen
   - Update EditLocationScreen

---

## API Considerations

### Server Requirements

The following fields may need server-side support:
- `Item.custom_fields: Map<String, Any>` - May already be supported
- `Location.room_category: String` - May need to be added to server API

### Backwards Compatibility

All new fields should be nullable with defaults:
```kotlin
val custom_fields: Map<String, Any>? = null
val room_category: String? = null
```

This ensures the app works with older server versions.

---

## Testing Checklist

### International Formats
- [ ] Currency displays correctly for different locales (US, EU, UK, etc.)
- [ ] Dates display in locale-appropriate format
- [ ] Handles edge cases (null values, invalid formats)

### Custom Fields
- [ ] Custom fields render when present in API response
- [ ] Empty/null custom_fields handled gracefully
- [ ] Various value types display correctly (string, number, boolean)

### Room Categories
- [ ] Category dropdown shows all options
- [ ] Selected category saves correctly
- [ ] Category displays with appropriate icon in detail view
- [ ] Works with existing locations (null category)

---

## Build Verification

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## Notes

1. **Locale Detection**: Uses device locale by default. Could be extended to use server-configured locale.

2. **Custom Fields Edit**: Phase 5 focuses on display only. Editing custom fields could be a future enhancement.

3. **Room Category Icons**: Some icons (Bed, Bathtub, Kitchen, etc.) may require `material-icons-extended` dependency (already included).

4. **Server Sync**: Room categories should ideally be validated against server-supported categories if the server maintains a list.
