# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.0] - 2026-05-13

### Phase 7: Security Hardening & Critical Bug Fixes

#### 🔒 Security
- **Encrypted credential storage (S1):** Access tokens and saved passwords are now stored using `EncryptedSharedPreferences` (AES-256-GCM) via the new `SecurePreferencesManager`. Plaintext credential storage in DataStore has been removed.
- **Demo credentials gated (S2):** Hardcoded demo credentials in `LoginScreen` are now wrapped in `BuildConfig.DEBUG` and will not appear in release builds.
- **Network security config (S3):** Replaced the global `usesCleartextTraffic="true"` manifest flag with a `network_security_config.xml` that permits HTTP only for `localhost` and private LAN ranges. All other traffic requires HTTPS.
- **OIDC PKCE & state validation (S4):** Added `PkceUtil` for PKCE code verifier/challenge generation and CSRF state tokens. The deep link handler in `MainActivity` now validates the `state` parameter and rejects mismatched OIDC callbacks.

#### 🐛 Bug Fixes
- **Settings data loss (B1):** `DashboardViewModel.saveSettings()` now uses `.copy()` to preserve printer configuration (model, density, label dimensions) when saving server settings.
- **Loading state leak (B3):** `deleteItem()` and `deleteLocation()` now reset `isLoading` in a `finally` block, preventing stuck spinners after navigation.
- **Silent exceptions (B4):** All silent `catch` blocks across ViewModels (`AddItemViewModel`, `EditItemViewModel`, `DashboardViewModel`, `MaintenanceViewModel`) now log via `Log.w` with contextual messages.
- **Bluetooth magic number (B5):** Replaced raw integer `2` with `BluetoothProfile.STATE_CONNECTED` in `ItemDetailViewModel`.

#### ♻️ Code Quality
- **Centralized URLs (M1):** Created `Constants.kt` with `DEFAULT_REMOTE_URL` and `DEFAULT_LOCAL_URL`. Hardcoded server URLs replaced across 5+ files.
- **PrinterViewModel flow fix (M2):** Changed `loadSettings()` from `collect()` to `first()` to prevent user edits from being overwritten by DataStore emissions.
- **Bitmap memory management (M3):** Added `recycle()` calls for intermediate QR code and rotation bitmaps in `LabelBitmapGenerator`.
- **Debug log gating (M4):** Google Client ID logging in `LoginViewModel` is now gated behind `BuildConfig.DEBUG`.
- **Typed API responses (M5):** Created `ResponseModels.kt` with `StatusResponse`, `MediaStatsResponse`, and `PrintLabelResponse` data classes, replacing all `Map<String, Any>` return types in `NesVentoryApi.kt`.
- **LoginScreen testability (M6):** Extracted `LoginScreenContent` composable for direct parameter-based testing without ViewModel dependency.

#### ⏳ Deferred
- **PrintJobExecutor extraction (B2):** The `printLabelLocally()` duplication across three ViewModels was not extracted in this phase. Deferred to Phase 8 (Architecture Refactor) where it will be addressed alongside the repository layer introduction.

#### New Files
- `app/src/main/java/com/tokendad/nesventory/data/preferences/SecurePreferencesManager.kt`
- `app/src/main/java/com/tokendad/nesventory/util/PkceUtil.kt`
- `app/src/main/java/com/tokendad/nesventory/util/Constants.kt`
- `app/src/main/java/com/tokendad/nesventory/data/remote/ResponseModels.kt`
- `app/src/main/res/xml/network_security_config.xml`

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
