package com.nesventory.android.data.api

import com.nesventory.android.data.model.AIStatus
import com.nesventory.android.data.model.DetectionResult
import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Location
import com.nesventory.android.data.model.MaintenanceTask
import com.nesventory.android.data.model.MediaBulkDeleteRequest
import com.nesventory.android.data.model.MediaListResponse
import com.nesventory.android.data.model.MediaStats
import com.nesventory.android.data.model.MediaUpdateRequest
import com.nesventory.android.data.model.PluginStatus
import com.nesventory.android.data.model.SystemStatus
import com.nesventory.android.data.model.Tag
import com.nesventory.android.data.model.TokenResponse
import com.nesventory.android.data.model.User
import com.nesventory.android.data.model.Video
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * NesVentory API service interface.
 * Defines all API endpoints for communication with the NesVentory backend.
 */
interface NesVentoryApi {

    /**
     * Login with email and password to get an access token.
     * Uses OAuth2 form-based authentication.
     */
    @FormUrlEncoded
    @POST("api/token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    /**
     * Get current user information.
     */
    @GET("api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<User>

    /**
     * Get all items.
     */
    @GET("api/items/")
    suspend fun getItems(
        @Header("Authorization") authorization: String
    ): Response<List<Item>>

    /**
     * Get all locations.
     */
    @GET("api/locations/")
    suspend fun getLocations(
        @Header("Authorization") authorization: String
    ): Response<List<Location>>

    /**
     * Get all tags.
     */
    @GET("api/tags/")
    suspend fun getTags(
        @Header("Authorization") authorization: String
    ): Response<List<Tag>>

    /**
     * Get all maintenance tasks.
     */
    @GET("api/maintenance/")
    suspend fun getMaintenanceTasks(
        @Header("Authorization") authorization: String
    ): Response<List<MaintenanceTask>>

    /**
     * Get all videos.
     */
    @GET("api/videos/")
    suspend fun getVideos(
        @Header("Authorization") authorization: String
    ): Response<List<Video>>

    /**
     * Get system status including version and feature flags.
     */
    @GET("api/status")
    suspend fun getSystemStatus(): Response<SystemStatus>

    /**
     * Get AI service status.
     */
    @GET("api/ai/status")
    suspend fun getAIStatus(
        @Header("Authorization") authorization: String
    ): Response<AIStatus>

    /**
     * Get plugin status and available plugins.
     */
    @GET("api/plugins/status")
    suspend fun getPluginStatus(
        @Header("Authorization") authorization: String
    ): Response<PluginStatus>

    /**
     * Analyze an uploaded image using AI to detect household items.
     * Returns a list of detected items with names, descriptions, and estimated values.
     */
    @Multipart
    @POST("api/ai/detect-items")
    suspend fun detectItems(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part
    ): Response<DetectionResult>

    // Media Management endpoints (v6.2.0+)

    /**
     * Get media statistics including total counts and storage info.
     */
    @GET("api/media/stats")
    suspend fun getMediaStats(): Response<MediaStats>

    /**
     * List all media with optional filtering.
     *
     * @param authorization Bearer token for authentication
     * @param locationFilter Filter by location name or ID
     * @param mediaType Filter by media type ('photo' or 'video')
     * @param unassignedOnly Only show media not assigned to any item (photos only)
     */
    @GET("api/media/list")
    suspend fun listMedia(
        @Header("Authorization") authorization: String,
        @Query("location_filter") locationFilter: String? = null,
        @Query("media_type") mediaType: String? = null,
        @Query("unassigned_only") unassignedOnly: Boolean = false
    ): Response<MediaListResponse>

    /**
     * Bulk delete media files.
     *
     * @param authorization Bearer token for authentication
     * @param request Bulk delete request with media IDs and types
     */
    @DELETE("api/media/bulk-delete")
    suspend fun bulkDeleteMedia(
        @Header("Authorization") authorization: String,
        @Body request: MediaBulkDeleteRequest
    ): Response<Unit>

    /**
     * Update media metadata.
     *
     * @param authorization Bearer token for authentication
     * @param mediaId The ID of the media to update
     * @param request Media update request with new metadata
     */
    @PATCH("api/media/{media_id}")
    suspend fun updateMedia(
        @Header("Authorization") authorization: String,
        @Path("media_id") mediaId: String,
        @Body request: MediaUpdateRequest
    ): Response<Unit>
}
