# Android App Implementation Plan (Targeting NesVentory v6.5.1)

This document outlines the roadmap for updating the NesventoryNew Android application to match the features and API changes introduced in NesVentory server versions up to v6.5.1.

## Phase 1: Core Connectivity & Authentication
**Goal:** Ensure stable connection with the latest server API and support new authentication methods.

- [ ] **API Endpoint Review:**
    - Verify `NetworkModule` matches the current API structure (Note: Versioning is not yet in URLs).
    - Handle potential changes in error responses (401/403) specifically for expired tokens/sessions.
- [ ] **OIDC & Authelia Support (v6.4.0):**
    - Investigate the new `/api/auth/oidc/*` endpoints.
    - Update `LoginScreen` and `LoginViewModel` to support an "SSO / OIDC Login" flow (likely via a web view or browser intent redirecting to the server's auth page).
    - Handle session persistence for OIDC-based tokens.
- [ ] **Document Host Checks (v6.5.1):**
    - Verify if the "relaxed document host checks" on the server side simplifies the `BASE_URL` configuration in the app (e.g., handling `localhost` vs LAN IP).

## Phase 2: AI & Smart Features Integration
**Goal:** Bring the new server-side AI capabilities to the mobile experience.

- [ ] **AI-Powered Item Enrichment (v6.1.2+):**
    - **UI:** Add an "Auto-Fill / Enrich" button in `AddItemScreen` and `EditItemScreen`.
    - **API:** Integrate `/api/items/{item_id}/enrich` (or the appropriate AI endpoint if it returns data directly).
- [ ] **Image-based Detection (v6.3.1):**
    - **Feature:** "Camera to AI" processing.
    - **UI:** Add a camera capture option in `AddItemScreen` specifically for "AI Scan".
    - **API:** Upload image to `/api/ai/detect-items` or `/api/ai/scan-barcode`.
    - **Logic:** Parse the response to pre-fill the "Add Item" form.
- [ ] **Barcode & Lookup Enhancements:**
    - Support `barcodelookup.com` provider integration (server-side config, but client might need to handle the data structure).
    - Integrate `/api/ai/barcode-lookup` for manual barcode entry lookups.

## Phase 3: Printer & Hardware Synchronization
**Goal:** Align local Bluetooth printing with server robustness and enable remote server printing.

- [ ] **Protocol Sync (v6.5.1):**
    - Review `BluetoothPrinterManager.kt` and `NiimbotProtocol.kt` against the server's recent "Fixed NIIMBOT D11-H printer support" (v6.5.1).
    - Ensure the "Magic Sequence" and 300 DPI vs 203 DPI handling is consistent with the server's findings (especially for D11-H vs D110).
- [ ] **Remote Server Printing:**
    - **Feature:** Allow the Android app to trigger a print job on a printer physically connected to the *server* (USB/Bluetooth).
    - **UI:** Add a "Print Method" selection in `PrinterSettingsScreen` (Local Bluetooth vs. Server Printer).
    - **API:** Integrate `/api/printer/print-label` and `/api/printer/config`.
    - **Config:** Allow selecting the target server printer model via `/api/printer/models`.

## Phase 4: Data & Management Updates
**Goal:** Expose new data fields and management tabs.

- [ ] **Media Management (v6.2.0):**
    - **UI:** Add a dedicated "Media" tab in `ItemDetailScreen`.
    - **Features:** View gallery, upload new photos/videos (`/api/media/list`, upload endpoint), delete media (`/api/media/bulk-delete`).
    - **Video:** Ensure video playback or thumbnail support for video assets.
- [ ] **Insurance Tab (v6.3.0):**
    - **UI:** Add an "Insurance" tab/section to `ItemDetailScreen`.
    - **Fields:** Display and edit insurance-related fields (Policy #, Value, Coverage type).
- [ ] **Maintenance Tracking (v6.0.0):**
    - **UI:** Update `MaintenanceScreen` to support new tracking fields.
    - **API:** Verify CRUD endpoints for maintenance records align with the v6.0.0 schema.

## Phase 5: UI/UX & Internationalization
**Goal:** Polish and global support.

- [ ] **International Formats (v6.5.0):**
    - **Currency:** Respect the server's or device's currency locale settings in `ItemDetail` and lists.
    - **Date/Time:** Use locale-aware date formatting throughout the app.
    - **Implementation:** Create `CurrencyFormatter` and `DateFormatter` utility classes.
- [ ] **Custom Fields:**
    - Support dynamic rendering of "Custom Fields" introduced in v6.5.0 if they are exposed in the Item API response.
    - Add `custom_fields: Map<String, Any>?` to Item model.
    - Create `CustomFieldsSection` composable for display.
- [ ] **Location Room Categories:**
    - **Feature:** Add room/area categorization for locations (Kitchen, Bedroom, Garage, Office, etc.).
    - **Model:** Add `room_category: String?` to Location and LocationCreate models.
    - **UI:** Add category selector dropdown in AddLocationScreen and EditLocationScreen.
    - **Display:** Show room category chip with icon in LocationDetailScreen.
    - **Categories:** Living Room, Bedroom, Kitchen, Bathroom, Dining Room, Office, Garage, Basement, Attic, Storage, Closet, Laundry Room, Outdoor, Other.

## Testing Strategy
- **Stage 1 & 2:** Can be tested with a local dev server instance.
- **Stage 3:** Requires physical hardware (Niimbot printers) and potentially a server setup with USB printer for remote print testing.
- **Stage 4 & 5:** UI/Integration testing.
