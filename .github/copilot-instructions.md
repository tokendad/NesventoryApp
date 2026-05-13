# Copilot Instructions for Nesventory

## Project Overview

This is the Android companion app for the **[NesVentory server](https://github.com/tokendad/NesVentory)** — a FastAPI-based home inventory backend. The API contract lives at [`docs/API-CONTRACT.md`](https://github.com/tokendad/NesVentory/blob/main/docs/API-CONTRACT.md) in the server repo and describes all endpoints, field shapes, breaking-change policy, and version history.

> **Rename note:** The project was previously named `NesventoryNew`. The old package ID `com.tokendad.nesventorynew` still appears in `.idea/appInsightsSettings.xml` (Play Store/App Insights connection only) — do not propagate it elsewhere.

## Build & Test Commands

```bash
./gradlew assembleDebug          # Debug APK
./gradlew bundleRelease          # Release AAB (needs signing env vars)
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (needs device/emulator)
./gradlew clean                  # Clean build

# Single test class
./gradlew test --tests "com.tokendad.nesventory.util.FormatterTest"
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
- `NesStates.kt` — composables for loading, empty, and error states (use instead of inline placeholders)

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

### Repository Layer

`data/repository/` defines interfaces (`ItemRepository`, `LocationRepository`, `MaintenanceRepository`, `PrinterRepository`, `SystemRepository`). Implementations live in `data/repository/impl/` and are bound to interfaces via Hilt's `@Binds` in `RepositoryModule.kt`. ViewModels inject the interface, never the implementation directly.

### Persistence

Two preference managers, both Hilt singletons:
- **`PreferencesManager`** — wraps DataStore Preferences for non-sensitive settings (`ServerSettings`, `PrinterConfig`). Exposes `Flow<T>`, updated via `suspend` functions.
- **`SecurePreferencesManager`** — wraps `EncryptedSharedPreferences` (AES-256-GCM) for sensitive data: access tokens and saved passwords. Inject this for anything credential-related.

Groups in `PreferencesManager`: `ServerSettings`, `UserSession`, `SavedCredentials`, `PrinterConfig`.

### Utility Classes

`util/` contains shared helpers:
- `CurrencyFormatter`, `DateFormatter` — display formatting
- `PkceUtil` — generates PKCE code verifier/challenge for OIDC auth
- `RoomCategories` — constants for location room categories
- `Constants` — `DEFAULT_REMOTE_URL`, `DEFAULT_LOCAL_URL`

### Security

- Sensitive data (tokens, passwords) goes in `SecurePreferencesManager` (EncryptedSharedPreferences AES-256-GCM), not DataStore
- `network_security_config.xml` restricts cleartext HTTP to `localhost` and private LAN ranges; all other traffic requires HTTPS
- OIDC auth uses PKCE (S256) via `PkceUtil` with state parameter validation
- Demo credentials are gated with `BuildConfig.DEBUG` and stripped from release builds

### API Contract

The server API is versioned and the contract is maintained in the server repo. Key rules for Android development:

- **Photo `path` is relative** — always prefix with the server base URL before loading with Coil
- **Fields marked ⚠️ in the contract were added post-v1** — handle their absence gracefully (`null`/empty defaults); do not assume presence
- **Authentication**: the app uses `Bearer` token via the OkHttp interceptor; `X-API-Key` is an alternative the server also supports
- **Breaking changes** trigger an issue in this repo — check `CHANGELOG.md` against the server's API Changes sections when updating models

Recent additions to be aware of when modifying data models:
- **v6.14** — `warranties` array on `Item`, `paint_info` array on `Location`
- **v6.15** — Living items: `is_living`, `birthdate`, `relationship_type`, `contact_info` fields on `Item`; people/pets are location-constrained to `Home`
- **v7.0.0** — Collections: new `/api/collections/` endpoints; `collection_id` / `collection_id_recursive` query params on `GET /api/items/`

### App Versioning

Version is read from the root `VERSION` file (currently `1.3.0`). `versionCode` comes from `VERSION_CODE` env var (defaults to 5).
