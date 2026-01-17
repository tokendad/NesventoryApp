# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2026-01-17

### Added
- **Room Categories**: Support for categorizing locations (e.g., Kitchen, Bedroom, Office) with visual icons.
- **Custom Fields**: Dynamic display of custom data fields for items.
- **Internationalization**: Locale-aware formatting for currency and dates throughout the application.
- **Phase 4 & 5 Complete**: Full implementation of Media Management, Insurance tracking, and Maintenance features.

### Changed
- Refactored `BluetoothPrinterManager` to support Android 13+ (API 33) Bluetooth APIs.
- Updated `DashboardViewModel` to handle `minSdk` 24 Wi-Fi scanning requirements correctly.
- Improved UI component usage (MenuAnchorType) to remove deprecation warnings.

### Fixed
- Fixed build warnings related to deprecated Bluetooth and UI APIs.
- Fixed lint errors regarding permission checks and constant types in Bluetooth manager.

