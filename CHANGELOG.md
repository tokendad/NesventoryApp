# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2026-01-17

### Added
- **Room Categories**: New location categories with icons.
- **Custom Fields**: Support for dynamic item fields.
- **Internationalization**: Localized currency and date formatting.
- **New Features**: Media Management, Insurance tracking, and Maintenance features.

### Improvements
- Android 13+ Bluetooth support and UI updates.
- Dashboard Wi-Fi scanning fixes.
- Resolved build warnings and lint errors.

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