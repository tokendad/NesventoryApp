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

## [1.2.0] - 2026-01-05

### Added
- **Niimbot Printer Support**: Full integration with Niimbot D110 and D11_H Bluetooth printers.
- **RFID Tag Reading**: Ability to read paper type, serial number, and barcode from printer consumables.
- **Smart Print Protocol**: Automatic switching between Standard (203 DPI) and High Density (300 DPI) print modes.
- **Bluetooth Manager**: Robust device discovery and connection handling with auto-reconnect.
- **Label Generator**: Dynamic generation of item labels with QR codes and icons.

### Changed
- Updated printer settings UI to show connected device status and RFID info.
- Improved error handling for Bluetooth write operations.

## [1.1.0] - 2025-12-23

### Added
- Native debug symbols for Play Store App Bundle.
- Multi-stage AI detection confirmation flow in Add Item screen.
- Option to accept or reject AI results with fallback to manual entry.
- Version tracking with `VERSION` file.
- Automated release and documentation workflows.

### Fixed
- Package name consistency across deployment workflows.

## [1.0.0] - 2024-12-22
- Initial release.