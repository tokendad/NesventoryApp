# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added - 5.0 Feature Integration
- **Location Enhancements:**
  - `LocationType` enum with values: RESIDENTIAL, COMMERCIAL, RETAIL, INDUSTRIAL, APARTMENT_COMPLEX, CONDO, MULTI_FAMILY, OTHER
  - Owner information support (`ownerInfo` field)
  - Landlord information support (`landlordInfo` field)
  - Tenant information support (`tenantInfo` field)
  - Insurance information support (`insuranceInfo` field)

- **Item Enhancements:**
  - `Warranty` data class with warranty type, provider, policy number, duration, and expiration
  - `Document` data class for document attachments
  - AI valuation tracking fields: `estimatedValueAiDate`, `estimatedValueUserDate`, `estimatedValueUserName`
  - Living item support with fields: `birthdate`, `contactInfo`, `relationshipType`, `isCurrentUser`, `associatedUserId`

- **User Enhancements:**
  - `UserRole` enum with values: ADMIN, EDITOR, VIEWER
  - Google OAuth support (`googleId` field)
  - API key authentication support (`apiKey` field)
  - AI scheduling configuration: `aiScheduleEnabled`, `aiScheduleIntervalDays`, `aiScheduleLastRun`
  - Google Drive backup tracking (`gdriveLastBackup` field)
  - UPC database configuration support (`upcDatabases` field)
  - User timestamps: `createdAt`, `updatedAt`

- **Documentation:**
  - Migration guide (MIGRATION_5.0.md) for upgrading to 5.0 features
  - Comprehensive CHANGELOG

### Changed
- **Build Configuration:**
  - Gradle wrapper downgraded from 8.13 to 8.7 (stable)
  - Android Gradle Plugin (AGP) set to 8.3.2 (stable, compatible with Gradle 8.7)
  - Kotlin downgraded from 2.2.21 to 1.9.23 (stable toolchain)
  - KSP updated to 1.9.23-1.0.20 (compatible with Kotlin 1.9.23)
  - Compose BOM downgraded from 2025.11.01 to 2024.06.00 (stable)

- **Networking Libraries:**
  - Retrofit downgraded from 3.0.0 to 2.11.0 (avoid breaking changes)
  - OkHttp downgraded from 5.3.2 to 4.12.0 (avoid breaking changes)

- **Other Dependencies:**
  - Hilt updated to 2.51.1
  - Lifecycle updated to 2.8.4
  - Activity Compose updated to 1.9.1
  - Navigation Compose updated to 2.7.7
  - Core KTX updated to 1.13.1
  - kotlinx-serialization updated to 1.6.3

- **Data Models (Breaking Changes):**
  - `Location.locationType`: Changed from `String?` to `LocationType?` enum
  - `Item.purchasePrice`: Changed from `String?` to `Double?`
  - `Item.estimatedValue`: Changed from `String?` to `Double?`
  - `User.role`: Changed from `String` to `UserRole` enum
  - `Tag`: Added `createdAt` and `updatedAt` timestamp fields

- **Documentation:**
  - README updated to reflect NesVentory v5.x compatibility
  - Target SDK updated to API 36 in documentation

### Deprecated
- Dependabot auto-updates temporarily disabled (.github/dependabot.yml renamed to .github/dependabot.yml.disabled)

### Security
- Type-safe enums introduced for constrained values (LocationType, UserRole, WarrantyType)
- Proper SerialName annotations ensure API compatibility
- All complex nested data uses JsonObject for flexibility and safety

## [0.1.0-alpha] - Initial Release

### Added
- Initial Android companion app for NesVentory
- Login with server settings configuration
- Auto URL switching based on WiFi SSID
- Inventory item viewing
- Location browsing
- Secure credential storage with DataStore
- Material 3 UI with dark mode support
- MVVM architecture with Repository pattern
- Hilt dependency injection
- Retrofit networking with Kotlin Serialization
- Comprehensive CI/CD with GitHub Actions
- Google Play deployment workflow

### Tech Stack
- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- Retrofit
- DataStore
- Minimum SDK: Android 8.0 (API 26)
- Target SDK: Android 15+ (API 36)

[Unreleased]: https://github.com/tokendad/Android-NesVentory/compare/v0.1.0-alpha...HEAD
[0.1.0-alpha]: https://github.com/tokendad/Android-NesVentory/releases/tag/v0.1.0-alpha
