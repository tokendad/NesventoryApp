# Server Connectivity Checking Implementation

This document describes the server connectivity checking feature added to the NesVentory Android app.

## Overview

The app now includes comprehensive error checking and network connection monitoring to indicate whether the backend server is available. This helps users understand connection issues and take appropriate action.

## Features

### 1. Connection Status States

Four connection states are tracked via the `ConnectionStatus` enum:

- **CONNECTED**: Server is reachable and responding (displayed with green checkmark icon)
- **DISCONNECTED**: Server is not reachable (displayed with red error icon)
- **NO_NETWORK**: No internet connection available (displayed with red WiFi-off icon)
- **NOT_CONFIGURED**: Server settings not configured (displayed with yellow warning icon)

### 2. Server Reachability Check

**Location**: `NetworkUtils.kt`

The `isServerReachable()` method:
- Attempts to connect to the server's `/api/status` endpoint
- Has a configurable timeout (default: 5 seconds)
- Runs on IO dispatcher to avoid blocking the main thread
- Returns `true` if the server responds with 2xx or 3xx status code

```kotlin
suspend fun isServerReachable(baseUrl: String, timeoutMs: Long = 5000): Boolean
```

### 3. Connection Status Checking

**Location**: `NesVentoryRepository.kt`

The `checkConnectionStatus()` method:
1. First checks if network is available
2. Verifies server settings are configured
3. Tests if the server is reachable
4. Returns the appropriate `ConnectionStatus`

Helper method `getConnectionStatusMessage()` provides user-friendly messages for each status.

### 4. Enhanced Error Handling

API methods now catch and handle specific network exceptions:
- `UnknownHostException`: "Cannot reach server - check your connection"
- `SocketTimeoutException`: "Server timeout - please try again"
- `ConnectException`: "Cannot connect to server"

This helps users understand the exact nature of connection problems.

### 5. UI Indicators

#### Login Screen
- Connection status card showing:
  - Current connection state with appropriate icon and color
  - Server URL being used
  - Refresh button to manually recheck connection
- Status is checked on screen load and after server settings changes

#### Dashboard Screen
- Connection status icon in the top app bar
  - Green checkmark when connected
  - Red error/WiFi-off icon when disconnected
- Connection info card in the main content area
  - Shows detailed connection information
  - Color-coded by status (green for local, blue for remote, red for errors)
- Status is checked when data is loaded

### 6. Connection Status Flow

1. **On Login Screen Load**:
   - Check connection status
   - Display status in the connection card
   - Users can tap refresh to recheck

2. **Before Login**:
   - Connection status is verified
   - Login fails with appropriate message if server is unreachable

3. **On Dashboard Load**:
   - Connection status checked before loading data
   - If not connected, shows error message and stops data loading
   - Connection status displayed in top bar and info card

4. **Manual Refresh**:
   - Pull-to-refresh triggers connection recheck and data reload
   - Refresh button in top bar forces data reload (includes connection check)

## Technical Details

### Threading
- All network operations run on `Dispatchers.IO`
- UI updates happen on the main thread via StateFlow

### Timeouts
- Default connection timeout: 5 seconds
- Can be configured via `timeoutMs` parameter

### Network Requirements
- Requires `INTERNET` permission (already in manifest)
- Requires `ACCESS_NETWORK_STATE` permission (already in manifest)

## User Experience

### Visual Feedback
- Color-coded status indicators:
  - Green/Blue: Connected (local/remote)
  - Red: Disconnected or no network
  - Yellow: Not configured
- Icons clearly communicate status at a glance
- Detailed status messages explain the situation

### Error Messages
- Specific error messages help users troubleshoot
- Distinguishes between network issues and server problems
- Provides actionable information

## Testing

Unit tests have been added to verify:
- Connection status enum values are defined
- All four status types exist
- Error handling works correctly

## Future Enhancements

Potential improvements:
1. Periodic background connection checking
2. Network state change listeners for real-time updates
3. Connection quality indicators (latency, bandwidth)
4. Offline mode with cached data
5. Automatic retry with exponential backoff
