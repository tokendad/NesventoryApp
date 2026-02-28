# NesventoryNew

An Android application for managing your NesVentory with AI-powered item detection.

## Features

- **Inventory Management**: Keep track of your items, their locations, brands, and values.
- **AI Item Detection**: Use your camera or gallery to automatically identify items and populate details.
- **Location Tracking**: Organize your items by physical locations with **Room Categories**.
- **Printer Support**: Native support for Niimbot Bluetooth printers (D11, D110, D11_H) with RFID tag reading.
- **Asset Management**:
    - **Media Gallery**: Attach photos and documents to items.
    - **Maintenance Tracking**: Schedule and track maintenance tasks for your equipment.
    - **Insurance Info**: Record policy details and coverage for valuable items.
- **Play Store Integrated**: Automated deployment workflows for internal testing.

## Security

NesventoryNew implements several layers of security hardening (Phase 7):

- **Encrypted Credential Storage**: Access tokens and passwords are stored using Android's `EncryptedSharedPreferences` (AES-256-GCM). Non-sensitive settings remain in standard DataStore.
- **Network Security Config**: Cleartext HTTP traffic is restricted to `localhost` and private LAN ranges (`10.x`, `172.16.x`, `192.168.x`) via `network_security_config.xml`. All other traffic requires HTTPS.
- **OIDC PKCE Flow**: OAuth/OIDC authentication uses PKCE (S256) with state parameter validation to prevent token injection and CSRF attacks.
- **Debug-Only Demo Credentials**: Demo login credentials are gated behind `BuildConfig.DEBUG` and are stripped from release builds.
- **Sensitive Log Gating**: Potentially sensitive information (e.g., client IDs) is only logged in debug builds.

For full details, see [`docs/Implementation/PHASE7_SECURITY_AND_BUGFIXES.md`](docs/Implementation/PHASE7_SECURITY_AND_BUGFIXES.md).

## Printer Support

NesVentory now supports printing item labels directly to Niimbot portable thermal printers.

### Supported Models
- **Niimbot D11-H**: 96px print head, 300 DPI (D110M_V4 Protocol).

### Features
- **Bluetooth Connectivity**: Auto-discovery and connection management.
- **RFID Tag Reading**: Automatically detects paper type, serial number, and remaining capacity from the printer's RFID tag.
- **Custom Labels**: Prints Item ID, Name, QR Code, and Icon.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- JDK 17.
- Android SDK 36 (target).

### Building

To build the project, run:

```bash
./gradlew assembleDebug
```

To build the release App Bundle:

```bash
./gradlew bundleRelease
```

## Workflows

The project uses GitHub Actions for CI/CD:
- **Deploy to Play Store**: Manually triggered workflow to build and upload the app to the Internal Testing track.
- **Release Management**: (Coming soon) Automated versioning and release notes.


## Server API

This app connects to a self-hosted [NesVentory](https://github.com/tokendad/NesVentory) server via its REST API.

- **API Contract**: [docs/API-CONTRACT.md](https://github.com/tokendad/NesVentory/blob/main/docs/API-CONTRACT.md) — field reference, breaking-change policy, and change log
- **Live spec**: `/api/openapi.json` on your running server (also at `/api/docs`)
- **API change notifications**: tracked as issues in this repo tagged "API Update"

## License

This project is licensed under the terms of the MIT license.
