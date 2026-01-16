# Phase 4: Data & Management Updates - Implementation Details

This document describes the Phase 4 implementation for the NesventoryNew Android application, targeting NesVentory server v6.5.1.

## Overview

Phase 4 adds three major features:
1. **Media Management** - Media tab in ItemDetailScreen
2. **Insurance Tab** - Insurance display in LocationDetailScreen (primary locations)
3. **Maintenance Tracking** - Create/delete tasks in MaintenanceScreen

---

## Feature 1: Media Management (ItemDetailScreen)

### Files Modified
- `ui/itemdetail/ItemDetailScreen.kt`

### Changes Made

1. **Added TabRow** with "Details" and "Media" tabs
2. **Refactored** existing content into `DetailsTab` composable
3. **Created `MediaTab`** composable with:
   - Photo gallery grid using `LazyVerticalGrid`
   - Displays all photos (not just primary)
   - "Primary" badge on primary photo with star icon
   - Empty state with `PhotoLibrary` icon when no photos

### UI Components

```
ItemDetailScreen
├── TopAppBar (title, back, print, delete)
├── TabRow ["Details", "Media"]
└── Content
    ├── Tab 0: DetailsTab
    │   ├── Primary Photo
    │   ├── Name, Brand/Model chips
    │   ├── Description
    │   ├── Serial Number
    │   ├── Purchase Info (price, date, retailer)
    │   ├── Estimated Value
    │   └── Timestamps
    └── Tab 1: MediaTab
        └── LazyVerticalGrid of photos with Primary badge
```

### Enhanced Details Tab
The DetailsTab now displays additional fields that were previously hidden:
- Serial Number
- Purchase Price
- Purchase Date
- Retailer

---

## Feature 2: Insurance Tab (LocationDetailScreen)

### Files Modified
- `ui/locationdetail/LocationDetailScreen.kt`

### Changes Made

1. **Conditional TabRow** - Only shows for locations where `is_primary_location == true`
2. **Refactored** existing content into `DetailsTab` composable
3. **Created `InsuranceTab`** composable with:
   - Company Section (name, address, phone, email, agent)
   - Policy Section (policy number)
   - Primary Holder Section (name, phone, email, address)
   - Additional Holders Section (multiple policy holders)
   - Property Details Section (purchase date, price, build date)

### UI Components

```
LocationDetailScreen
├── TopAppBar (title, back, print, delete)
├── TabRow ["Details", "Insurance"] (only if is_primary_location)
└── Content
    ├── Tab 0: DetailsTab (existing location details)
    └── Tab 1: InsuranceTab
        ├── InsuranceSection: "Insurance Company"
        │   └── company_name, company_address, company_phone, company_email, agent_name
        ├── InsuranceSection: "Policy Information"
        │   └── policy_number
        ├── InsuranceSection: "Primary Policy Holder"
        │   └── name, phone, email, address
        ├── InsuranceSection: "Additional Policy Holders" (if any)
        │   └── List of PolicyHolder details
        └── InsuranceSection: "Property Details"
            └── purchase_date, purchase_price, build_date
```

### Helper Composables
- `InsuranceSection` - ElevatedCard with icon and title
- `InsuranceField` - Label/value row display
- `PolicyHolderContent` - Displays all PolicyHolder fields

---

## Feature 3: Maintenance Tracking (MaintenanceScreen)

### Files Modified
- `ui/maintenance/MaintenanceScreen.kt`
- `ui/maintenance/MaintenanceViewModel.kt`

### Changes Made

#### MaintenanceViewModel
New state and methods:
```kotlin
// Filter state
var filterState by mutableStateOf("all") // all, pending, completed

// Create dialog state
var showCreateDialog by mutableStateOf(false)
var availableItems by mutableStateOf<List<Item>>(emptyList())
var newTaskTitle by mutableStateOf("")
var newTaskDescription by mutableStateOf("")
var newTaskItemId by mutableStateOf<UUID?>(null)
var newTaskDueDate by mutableStateOf("")
var newTaskFrequency by mutableStateOf<String?>(null)
var newTaskColor by mutableStateOf<String?>(null)

// Delete confirmation
var taskToDelete by mutableStateOf<MaintenanceTask?>(null)

// Computed property
val filteredTasks: List<MaintenanceTask>

// Methods
fun createTask()
fun deleteTask(task: MaintenanceTask)
fun resetCreateForm()
```

#### MaintenanceScreen
1. **Filter Chips** - All, Pending, Completed filters below TopAppBar
2. **FAB** - Floating action button to create new task
3. **Create Dialog** with fields:
   - Title (required)
   - Description (optional)
   - Item dropdown (required)
   - Due Date (required, format: YYYY-MM-DD)
   - Frequency dropdown (None, Daily, Weekly, Monthly, Yearly)
   - Color picker (6 preset colors)
4. **Delete Confirmation Dialog** - Confirms before deleting
5. **Enhanced Task Row**:
   - Frequency badge displayed
   - Delete button (red trash icon)
   - Improved empty state with icon

### UI Components

```
MaintenanceScreen
├── TopAppBar (title, exit button)
├── FilterChips [All, Pending, Completed]
├── LazyColumn
│   └── MaintenanceTaskRow
│       ├── Status Icon (checkmark or calendar)
│       ├── Title + Frequency Badge
│       ├── Due Date
│       ├── Description
│       ├── Delete Button
│       └── Completion Checkbox
├── FAB (Add Task)
├── CreateMaintenanceTaskDialog
│   ├── Title TextField
│   ├── Description TextField
│   ├── Item Dropdown
│   ├── Due Date TextField
│   ├── Frequency Dropdown
│   ├── Color Picker (FilterChips)
│   └── Create/Cancel buttons
└── Delete Confirmation Dialog
```

---

## API Endpoints Used

| Feature | Endpoint | Method |
|---------|----------|--------|
| Create Task | `api/maintenance` | POST |
| Delete Task | `api/maintenance/{task_id}` | DELETE |
| Get Items | `api/items/` | GET |
| Get Tasks | `api/maintenance` | GET |
| Update Task | `api/maintenance/{task_id}` | PUT |

---

## Data Models

### InsuranceInfo (LocationModels.kt)
```kotlin
data class InsuranceInfo(
    val company_name: String? = null,
    val company_address: String? = null,
    val company_email: String? = null,
    val company_phone: String? = null,
    val agent_name: String? = null,
    val policy_number: String? = null,
    val primary_holder: PolicyHolder? = null,
    val additional_holders: List<PolicyHolder> = emptyList(),
    val purchase_date: String? = null,
    val purchase_price: Double? = null,
    val build_date: String? = null
)
```

### MaintenanceTaskCreate (ItemModels.kt)
```kotlin
data class MaintenanceTaskCreate(
    val item_id: UUID,
    val title: String,
    val description: String? = null,
    val due_date: String,
    val frequency: String? = null,
    val color: String? = null
)
```

---

## Testing Checklist

### MaintenanceScreen
- [ ] Create a new maintenance task with all fields
- [ ] Verify task appears in list
- [ ] Toggle task completion
- [ ] Delete a task with confirmation
- [ ] Test filter chips (All/Pending/Completed)
- [ ] Verify frequency badge displays

### LocationDetailScreen
- [ ] View a primary location - Insurance tab should appear
- [ ] View a non-primary location - No tabs, just details
- [ ] Check InsuranceInfo fields display correctly
- [ ] Verify empty state when no insurance info

### ItemDetailScreen
- [ ] View an item - Details and Media tabs appear
- [ ] Check Media tab shows all photos
- [ ] Verify Primary badge on primary photo
- [ ] Test empty state when no photos

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

1. **Insurance Tab Location**: Insurance information is associated with Locations (not Items) in the data model. The Insurance tab only appears on primary locations.

2. **Media Tab Read-Only**: The Media tab in ItemDetailScreen is currently read-only (view photos). Upload/delete functionality exists in EditItemScreen.

3. **Date Input**: Due date in create task dialog uses simple text input with format hint. A date picker could be added as future enhancement.

4. **Color Picker**: Uses preset colors as FilterChips. A full color picker could be added later.
