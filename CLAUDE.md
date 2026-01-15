# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release AAB (requires signing config via env vars)
./gradlew bundleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.tokendad.nesventorynew.ExampleTest"

# Clean build
./gradlew clean
```

## Architecture Overview

NesventoryNew is a Kotlin Android app using Jetpack Compose, Hilt DI, and MVVM architecture. It manages inventory items with AI-powered detection and supports Niimbot Bluetooth label printers.

### Package Structure

```
com.tokendad.nesventorynew/
├── NesVentoryApplication.kt    # Hilt root (@HiltAndroidApp)
├── MainActivity.kt             # Single activity with NavHost
├── MainViewModel.kt            # Auth state, OIDC handling
├── AppModule.kt                # Hilt: PreferencesManager singleton
├── NetworkModule.kt            # Hilt: Retrofit, OkHttp, Bluetooth, LabelGenerator
├── data/
│   ├── preferences/PreferencesManager.kt   # DataStore for all persistent state
│   └── remote/
│       ├── NesVentoryApi.kt    # Retrofit interface (all endpoints)
│       ├── ItemModels.kt       # Item, ItemCreate, DetectionResult
│       ├── LocationModels.kt   # Location, LocationCreate
│       └── PrinterModels.kt    # Printer config, RFID models
└── ui/
    └── [screen]/               # Each screen has Screen.kt + ViewModel.kt
```

### Key Architectural Patterns

**MVVM with Compose State:** Each screen has a ViewModel using `mutableStateOf` for reactive UI. ViewModels are `@HiltViewModel` annotated.

**OkHttp Interceptor Chain (NetworkModule.kt):**
1. Host Selection Interceptor - Rewrites placeholder URL to actual server (remote/local based on WiFi)
2. Auth Header Interceptor - Injects Bearer token from PreferencesManager
3. 401 Handler Interceptor - Clears session on auth failure
4. Logging Interceptor

**Dual Server Routing:** The app supports both remote (internet) and local (LAN) server URLs. `PreferencesManager` stores `localSsid` and routing logic checks current WiFi to decide which URL to use.

### Navigation

Routes defined in `MainActivity.kt` with `NavHost`. Key routes:
- `login`, `server_settings`, `printer_settings`
- `main` (bottom nav hub with dashboard, items, locations, maintenance, server tabs)
- `item_details/{itemId}`, `location_details/{locationId}`
- `add_item`, `edit_item/{itemId}`, `add_location`, `edit_location/{locationId}`

### Printer Integration (Niimbot)

Located in `ui/printer/`:
- **NiimbotProtocol.kt** - Binary protocol implementation (D110M_V4), packet framing, RFID parsing
- **BluetoothPrinterManager.kt** - BLE scanning, GATT connections, command writing
- **LabelBitmapGenerator.kt** - Creates thermal-printer bitmaps with QR codes (ZXing)
- **PrinterViewModel.kt** - Device discovery, config persistence, print method selection

Supported model: D11-H (96px width, 300 DPI, D110M_V4 Protocol)

### Data Persistence (PreferencesManager.kt)

Uses DataStore Preferences with Flow-based API:
- `ServerSettings`: apiToken, remoteUrl, localUrl, localSsid, prioritizeLocal, theme, printMethod
- `UserSession`: accessToken, isLoggedIn
- `SavedCredentials`: username, password, isRemembered
- `PrinterConfig`: model, density, label dimensions

### API Integration

Backend is FastAPI-based (OAuth2 compatible). Key endpoint groups in `NesVentoryApi.kt`:
- Auth: `/api/auth/token`, `/api/auth/me`
- Items: CRUD at `/api/items/`
- Locations: CRUD at `/api/locations/`
- AI: `/api/ai/detect-items`, `/api/ai/barcode-lookup`, `/api/ai/parse-data-tag`
- Media: `/api/items/{id}/photos`, `/api/items/{id}/documents`
- Printers: `/api/printers/`, print jobs

### Dependencies (gradle/libs.versions.toml)

- Kotlin 2.0.21, Compose BOM 2024.09.00
- Hilt 2.51.1 for DI
- Retrofit 2.9.0 + OkHttp 4.12.0 for networking
- Navigation Compose 2.8.5
- Coil 2.7.0 for image loading
- ZXing 3.5.3 for QR codes
