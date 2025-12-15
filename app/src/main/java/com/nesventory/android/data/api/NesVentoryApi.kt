package com.nesventory.android.data.api

import com.nesventory.android.data.model.AIStatus
import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Location
import com.nesventory.android.data.model.MaintenanceTask
import com.nesventory.android.data.model.PluginStatus
import com.nesventory.android.data.model.SystemStatus
import com.nesventory.android.data.model.Tag
import com.nesventory.android.data.model.TokenResponse
import com.nesventory.android.data.model.User
import com.nesventory.android.data.model.Video
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    /**
     * Get current user information.
     */
    @GET("users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<User>

    /**
     * Get all items.
     */
    @GET("items/")
    suspend fun getItems(
        @Header("Authorization") authorization: String
    ): Response<List<Item>>

    /**
     * Get all locations.
     */
    @GET("locations/")
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
}
