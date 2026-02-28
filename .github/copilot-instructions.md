# Copilot Instructions for NesventoryNew

## Build & Test Commands

```bash
./gradlew assembleDebug          # Debug APK
./gradlew bundleRelease          # Release AAB (needs signing env vars)
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (needs device/emulator)
./gradlew clean                  # Clean build

# Single test class
./gradlew test --tests "com.tokendad.nesventorynew.util.FormatterTest"
```

Release signing requires `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` environment variables plus an `app/release.keystore` file.

## Architecture

Kotlin Android app using **Jetpack Compose + Hilt DI + MVVM**. Single-activity architecture with Compose Navigation.

### Dual Server Routing

The app connects to either a remote (internet) or local (LAN) backend URL depending on the device's current WiFi network. `PreferencesManager` stores the local SSID and both URLs; an OkHttp interceptor in `NetworkModule.kt` rewrites a placeholder base URL at request time.

### OkHttp Interceptor Chain (order matters)

1. **Host Selection** — rewrites placeholder URL to actual remote/local server
2. **Auth Header** — injects `Bearer` token from `PreferencesManager`
3. **401 Handler** — clears session on auth failure
4. **Logging** — logs the final rewritten request

### Navigation

Routes are defined as constants in a `Routes` object inside `MainActivity.kt`. The app supports deep links via `nesventory://` scheme and `/api/items/` paths.

### Printer Integration

`ui/printer/` implements the Niimbot D11-H binary protocol (D110M_V4) over BLE. This is low-level byte-oriented code — `NiimbotProtocol.kt` handles packet framing and RFID parsing, `BluetoothPrinterManager.kt` handles GATT connections.

## Key Conventions

### Screen Pattern

Every screen follows `ui/{feature}/` with two files:
- `{Feature}Screen.kt` — Composable UI
- `{Feature}ViewModel.kt` — `@HiltViewModel` with constructor injection

### ViewModel State

ViewModels use `mutableStateOf` (not StateFlow) for reactive Compose state. Standard fields: `isLoading: Boolean`, `errorMessage: String?`. Async work uses `viewModelScope.launch`.

### Reusable Components ("Nes" Prefix)

All shared UI components in `ui/components/` use the `Nes` prefix:
- `NesPrimaryButton`, `NesSecondaryButton`, `NesDestructiveButton`, `NesTextButton`
- `NesTextField`, `NesPasswordField`, `NesSearchField`, `NesTextArea`
- `NesCard`, `NesSectionCard`, `NesListItemCard`

New reusable components must follow this naming convention.

### Spacing & Sizing

Use the centralized spacing system, not hardcoded `dp` values:
- `NesSpacing.xs` (4dp), `.sm` (8dp), `.md` (12dp), `.lg` (16dp), `.xl` (24dp), `.xxl` (32dp)
- `NesSize.buttonHeight`, `.iconDefault`, `.thumbnailSmall`, `.minTouchTarget`, etc.

### Data Models

- Data classes use **snake_case** field names matching the FastAPI backend JSON directly (Gson handles serialization)
- Separate models for read vs. write: `Item` (from API) vs. `ItemCreate` (to API)
- Optional fields are nullable with defaults: `val brand: String? = null`

### API Layer

`NesVentoryApi.kt` is a single Retrofit interface for all endpoints. All methods are `suspend fun`. The base URL is a placeholder (`https://placeholder.local/`) rewritten by the interceptor chain.

### Dependency Management

Dependencies are declared in `gradle/libs.versions.toml` using version catalogs. Reference them as `libs.{name}` in `build.gradle.kts`.

### Persistence

`PreferencesManager` (Hilt singleton) wraps DataStore Preferences. Settings are exposed as `Flow<T>` and updated via `suspend` functions. Groups: `ServerSettings`, `UserSession`, `SavedCredentials`, `PrinterConfig`.

### App Versioning

Version is read from the root `VERSION` file (currently `1.3.0`). `versionCode` comes from `VERSION_CODE` env var (defaults to 5).
