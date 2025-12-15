# Migration Guide: NesVentory 6.0 API Integration

This document describes the changes made to integrate NesVentory 6.0 features into the Android application.

## Overview

The Android-NesVentory app has been updated to support the new features from NesVentory 6.0, including:
- Video management for items
- Maintenance tracking system
- LLM Plugin system support
- Enhanced system status and monitoring
- AI service integration

## Breaking Changes

**None.** Version 6.0 is fully backward compatible with 5.x APIs. All new features are additive and optional.

## New Features

### 🎥 Video Support

Items can now have video attachments in addition to photos and documents.

**New Model:**
```kotlin
data class Video(
    val id: String,
    val itemId: String,
    val filename: String,
    val mimeType: String?,
    val path: String,
    val uploadedAt: String
)
```

**API Endpoint:**
- `GET /api/videos/` - Retrieve all videos

**Item Model Update:**
- Added `videos: List<Video>` field to the Item model

### 🛠️ Maintenance Management

Track maintenance tasks for items and locations with recurring schedules.

**New Model:**
```kotlin
data class MaintenanceTask(
    val id: String,
    val title: String,
    val description: String?,
    val itemId: String?,
    val locationId: String?,
    val dueDate: String?,
    val completedAt: String?,
    val priority: MaintenancePriority?,
    val status: MaintenanceStatus,
    val recurrencePattern: String?,
    val color: String?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)
```

**Enums:**
- `MaintenancePriority`: LOW, MEDIUM, HIGH
- `MaintenanceStatus`: PENDING, IN_PROGRESS, COMPLETED, CANCELLED

**API Endpoint:**
- `GET /api/maintenance/` - Retrieve all maintenance tasks

### 🔌 Plugin System Support

Query available LLM plugins and their status.

**New Models:**
```kotlin
data class Plugin(
    val id: String,
    val name: String,
    val description: String?,
    val version: String?,
    val endpoint: String,
    val apiKey: String?,
    val priority: Int,
    val enabled: Boolean,
    val supportsImageProcessing: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class PluginStatus(
    val configured: Boolean,
    val plugins: List<Plugin>
)
```

**API Endpoint:**
- `GET /api/plugins/status` - Check plugin system configuration

### 🤖 AI Service Integration

Check if AI features (Gemini or plugins) are configured.

**New Model:**
```kotlin
data class AIStatus(
    val configured: Boolean,
    val geminiConfigured: Boolean,
    val pluginConfigured: Boolean
)
```

**API Endpoint:**
- `GET /api/ai/status` - Check AI service status

### ⚙️ System Status

Retrieve system information and feature flags.

**New Model:**
```kotlin
data class SystemStatus(
    val version: String,
    val aiConfigured: Boolean,
    val gdriveConfigured: Boolean,
    val pluginsConfigured: Boolean,
    val databaseType: String?
)
```

**API Endpoint:**
- `GET /api/status` - Get system status

### 🏷️ Tags Support

Tags can now be queried directly from the API.

**API Endpoint:**
- `GET /api/tags/` - Retrieve all tags

## Repository Updates

All new endpoints have been added to `NesVentoryRepository` with proper error handling:

```kotlin
suspend fun getTags(): ApiResult<List<Tag>>
suspend fun getMaintenanceTasks(): ApiResult<List<MaintenanceTask>>
suspend fun getVideos(): ApiResult<List<Video>>
suspend fun getSystemStatus(): ApiResult<SystemStatus>
suspend fun getAIStatus(): ApiResult<AIStatus>
suspend fun getPluginStatus(): ApiResult<PluginStatus>
```

## Backward Compatibility

All new features are optional and designed to work seamlessly with:
- NesVentory 6.x - Full feature support
- NesVentory 5.x - Core features work, new endpoints may return 404
- NesVentory 4.x - Not recommended, missing critical 5.0 data models

## UI Considerations

While the API integration is complete, the following UI features are recommended for future implementation:

### Dashboard Enhancements
- Display system status (version, features enabled)
- Show maintenance tasks due/overdue count
- Display AI and plugin configuration status

### Item Details Screen
- Video gallery alongside photos
- Video playback support
- Maintenance task association

### New Screens (Future)
- Maintenance Calendar - View and manage maintenance tasks
- System Settings - Display and configure system features
- Video Gallery - Browse and manage all videos

## Testing Recommendations

1. **API Integration:**
   - Test against NesVentory 6.0 backend
   - Verify graceful handling of missing/disabled features
   - Test error handling for unavailable endpoints

2. **Data Models:**
   - Test parsing of all new model types
   - Verify enum serialization
   - Test null handling for optional fields

3. **Backward Compatibility:**
   - Test with NesVentory 5.x servers
   - Ensure app doesn't crash on 404 responses
   - Verify core features work without 6.0 endpoints

## Version Information

- **App Version:** 0.2.0-alpha
- **Compatible Backend:** NesVentory 6.0.0+
- **Minimum Backend:** NesVentory 5.0.0

## Support

For questions or issues related to this migration:
- [Android App Issues](https://github.com/tokendad/Android-NesVentory/issues)
- [NesVentory Server Issues](https://github.com/tokendad/NesVentory/issues)
