# Migration Guide: NesVentory 5.0 Feature Integration

This document describes the changes made to integrate NesVentory 5.0 features into the Android application.

## Overview

The Android-NesVentory app has been updated to support the enhanced features from NesVentory 5.0, including:
- Enhanced location management with types and detailed metadata
- Warranty and document tracking for items
- Living item support (people, pets)
- AI-powered valuation tracking
- Property management features (owner, landlord, tenant info)
- Insurance information tracking

## Breaking Changes

### Data Model Changes

#### Location Model
**Changed Fields:**
- `locationType`: Changed from `String?` to `LocationType?` (enum)
  - **Migration**: String values will need to be mapped to enum values
  - Valid values: `RESIDENTIAL`, `COMMERCIAL`, `RETAIL`, `INDUSTRIAL`, `APARTMENT_COMPLEX`, `CONDO`, `MULTI_FAMILY`, `OTHER`

**New Fields:**
- `ownerInfo: JsonObject?` - Property owner details
- `landlordInfo: JsonObject?` - Landlord information
- `tenantInfo: JsonObject?` - Tenant information  
- `insuranceInfo: JsonObject?` - Insurance policy details

#### Item Model
**Changed Fields:**
- `purchasePrice`: Changed from `String?` to `Double?`
- `estimatedValue`: Changed from `String?` to `Double?`
  - **Migration**: String values will need to be parsed to Double

**New Fields:**
- `estimatedValueAiDate: String?` - Date when AI estimated the value
- `estimatedValueUserDate: String?` - Date when user supplied the value
- `estimatedValueUserName: String?` - Username who supplied the value
- `birthdate: String?` - For living items (people, pets)
- `contactInfo: JsonObject?` - Contact details for living items
- `relationshipType: String?` - Relationship type for living items
- `isCurrentUser: Boolean` - Flag for "current user" living items
- `associatedUserId: String?` - Associated user ID for living items
- `warranties: List<Warranty>` - Warranty information
- `documents: List<Document>` - Document attachments

**New Data Classes:**
- `Warranty` - Represents warranty information with type (MANUFACTURER, EXTENDED)
- `Document` - Represents attached documents

**Updated Classes:**
- `Tag` - Added `createdAt` and `updatedAt` timestamps

#### User Model
**Changed Fields:**
- `role`: Changed from `String` to `UserRole` (enum)
  - **Migration**: String values will need to be mapped to enum values
  - Valid values: `ADMIN`, `EDITOR`, `VIEWER`

**New Fields:**
- `googleId: String?` - Google OAuth ID
- `apiKey: String?` - API authentication key
- `aiScheduleEnabled: Boolean` - AI schedule enabled flag
- `aiScheduleIntervalDays: Int` - AI schedule interval (default: 7)
- `aiScheduleLastRun: String?` - Last AI schedule run timestamp
- `gdriveLastBackup: String?` - Last Google Drive backup timestamp
- `upcDatabases: JsonArray?` - UPC database configuration
- `createdAt: String?` - User creation timestamp
- `updatedAt: String?` - User update timestamp

## New Enums

### LocationType
Defines the type of location:
```kotlin
enum class LocationType {
    RESIDENTIAL,      // Single-family home
    COMMERCIAL,       // Commercial building
    RETAIL,          // Retail store
    INDUSTRIAL,      // Industrial facility
    APARTMENT_COMPLEX, // Multi-unit apartment building
    CONDO,           // Condominium
    MULTI_FAMILY,    // Multi-family dwelling
    OTHER            // Other location type
}
```

### UserRole
Defines user permission levels:
```kotlin
enum class UserRole {
    ADMIN,   // Full access
    EDITOR,  // Can edit content
    VIEWER   // Read-only access
}
```

### WarrantyType
Defines warranty types:
```kotlin
enum class WarrantyType {
    MANUFACTURER,  // Manufacturer warranty
    EXTENDED       // Extended/purchased warranty
}
```

## JSON Object Structures

### Location.ownerInfo
```json
{
  "owner_name": "John Doe",
  "spouse_name": "Jane Doe",
  "contact_info": "john@example.com",
  "notes": "Primary residence"
}
```

### Location.landlordInfo
```json
{
  "name": "Property Manager",
  "company": "PM Company LLC",
  "phone": "555-1234",
  "email": "pm@company.com",
  "address": "123 Main St",
  "notes": "Call during business hours"
}
```

### Location.tenantInfo
```json
{
  "name": "Tenant Name",
  "phone": "555-5678",
  "email": "tenant@example.com",
  "lease_start": "2024-01-01",
  "lease_end": "2024-12-31",
  "rent_amount": 1500.00,
  "notes": "Good tenant"
}
```

### Location.insuranceInfo
```json
{
  "company_name": "Insurance Co",
  "policy_number": "POL-123456",
  "contact_info": "agent@insurance.com",
  "coverage_amount": 500000.00,
  "notes": "Renews annually"
}
```

### Item.contactInfo (for living items)
```json
{
  "phone": "555-9876",
  "email": "person@example.com",
  "address": "456 Oak Ave",
  "notes": "Emergency contact"
}
```

## Code Migration Examples

### Handling LocationType Enum
**Old Code:**
```kotlin
val type = location.locationType // String?
if (type == "residential") {
    // ...
}
```

**New Code:**
```kotlin
val type = location.locationType // LocationType?
if (type == LocationType.RESIDENTIAL) {
    // ...
}
```

### Handling Price/Value as Double
**Old Code:**
```kotlin
val price = item.purchasePrice // String?
val displayPrice = price ?: "N/A"
```

**New Code:**
```kotlin
val price = item.purchasePrice // Double?
val displayPrice = price?.let { "$${"%.2f".format(it)}" } ?: "N/A"
```

### Handling UserRole Enum
**Old Code:**
```kotlin
val isAdmin = user.role == "admin"
```

**New Code:**
```kotlin
val isAdmin = user.role == UserRole.ADMIN
```

### Accessing Owner Information
```kotlin
val ownerName = location.ownerInfo
    ?.get("owner_name")
    ?.jsonPrimitive
    ?.content
```

## UI Considerations

### New Fields to Display

**Location Detail Screen:**
- Location type badge/chip
- Owner information section
- Landlord information (for rental properties)
- Tenant information (for multi-family/apartment)
- Insurance information

**Item Detail Screen:**
- Warranty information list
- Document attachments list
- AI valuation date and source
- Living item fields (birthdate, contact info, relationship)

**User Settings Screen:**
- AI schedule configuration
- Google Drive backup status
- UPC database priority configuration

## Backward Compatibility

All new fields are nullable, ensuring backward compatibility with older NesVentory backend versions:
- Old backends will simply omit new fields
- New backends will provide enhanced data
- The app gracefully handles missing fields

## Testing Recommendations

1. **Model Serialization:**
   - Test parsing responses with all new fields present
   - Test parsing responses with new fields absent
   - Test enum parsing with valid and invalid values

2. **Type Safety:**
   - Verify enum type checking works correctly
   - Test numeric value formatting (prices, values)

3. **API Integration:**
   - Test against NesVentory 4.x backend (should work)
   - Test against NesVentory 5.x backend (with new features)

4. **UI Updates:**
   - Verify new fields display correctly
   - Test form inputs for new fields
   - Ensure graceful fallback when data is missing

## Future Enhancements

Potential UI improvements to fully leverage new features:
- Location type filter/search
- Warranty expiration notifications
- Document viewer/manager
- Living item birthday reminders
- Property management dashboard
- Insurance policy renewal reminders
- AI valuation scheduler configuration UI

## Support

For questions or issues related to this migration:
- [Android App Issues](https://github.com/tokendad/Android-NesVentory/issues)
- [NesVentory Server Issues](https://github.com/tokendad/NesVentory/issues)
