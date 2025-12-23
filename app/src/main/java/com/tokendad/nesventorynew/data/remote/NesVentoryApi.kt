package com.tokendad.nesventorynew.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.UUID

// Data model for the Token response
data class LoginResponse(
    val access_token: String,
    val token_type: String
)

@Suppress("unused")
interface NesVentoryApi {

    /**
     * OAuth2 compatible token login endpoint.
     * Uses Form-URL-Encoded data (standard for FastAPI OAuth2PasswordRequestForm).
     */
    @FormUrlEncoded
    @POST("api/token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    /**
     * Get system status including health and version.
     */
    @GET("api/status")
    suspend fun getStatus(): Map<String, Any>

    /**
     * Get media statistics (Total counts, etc.)
     */
    @GET("api/media/stats")
    suspend fun getMediaStats(): Map<String, Any>
    /**
     * Get Items List
     */

    @GET("api/items/")
    suspend fun getItems(): List<Item>

    /**
     * Create a new Item
     */
    @POST("api/items/")
    suspend fun createItem(@Body item: ItemCreate): Item

    /**
     * Detect Items from Image
     */
    @Multipart
    @POST("api/ai/detect-items")
    suspend fun detectItems(@Part file: MultipartBody.Part): DetectionResult

    /**
     * Get Single Item Details
     */
    @GET("api/items/{id}")
    suspend fun getItem(@Path("id") id: UUID): Item

    /**
     * Delete an Item
     */
    @DELETE("api/items/{id}")
    suspend fun deleteItem(@Path("id") id: UUID)

    /**
     * Update an Item
     */
    @POST("api/items/{id}")
    suspend fun updateItem(@Path("id") id: UUID, @Body item: ItemCreate): Item

    /**
     * Enrich Item details via AI
     */
    @POST("api/ai/enrich-item/{id}")
    suspend fun enrichItem(@Path("id") id: UUID): Item

    /**
     * Maintenance Tasks
     */
    @GET("api/maintenance")
    suspend fun getMaintenanceTasks(): List<MaintenanceTask>

    @POST("api/maintenance")
    suspend fun createMaintenanceTask(@Body task: MaintenanceTaskCreate): MaintenanceTask

    @GET("api/maintenance/item/{item_id}")
    suspend fun getMaintenanceTasksForItem(@Path("item_id") itemId: UUID): List<MaintenanceTask>

    @GET("api/maintenance/{task_id}")
    suspend fun getMaintenanceTask(@Path("task_id") taskId: UUID): MaintenanceTask

    @PUT("api/maintenance/{task_id}")
    suspend fun updateMaintenanceTask(@Path("task_id") taskId: UUID, @Body task: MaintenanceTaskUpdate): MaintenanceTask

    @DELETE("api/maintenance/{task_id}")
    suspend fun deleteMaintenanceTask(@Path("task_id") taskId: UUID)

    /**
     * Media Management
     */
    @Multipart
    @POST("api/items/{item_id}/photos")
    suspend fun uploadItemPhoto(
        @Path("item_id") itemId: UUID,
        @Part file: MultipartBody.Part,
        @Part("is_primary") isPrimary: Boolean = false,
        @Part("is_data_tag") isDataTag: Boolean = false,
        @Part("photo_type") photoType: String? = null
    ): Photo

    @DELETE("api/items/{item_id}/photos/{photo_id}")
    suspend fun deleteItemPhoto(
        @Path("item_id") itemId: UUID,
        @Path("photo_id") photoId: UUID
    )

    @Multipart
    @POST("api/items/{item_id}/documents")
    suspend fun uploadItemDocument(
        @Path("item_id") itemId: UUID,
        @Part file: MultipartBody.Part,
        @Part("document_type") documentType: String? = null
    ): Document

    @DELETE("api/items/{item_id}/documents/{document_id}")
    suspend fun deleteItemDocument(
        @Path("item_id") itemId: UUID,
        @Path("document_id") documentId: UUID
    )

    /**
     * Get Locations List
     */
    @GET("api/locations/")
    suspend fun getLocations(): List<Location>

    /**
     * Create a new Location
     */
    @POST("api/locations/")
    suspend fun createLocation(@Body location: LocationCreate): Location

    /**
     * Get Single Location Details
     */
    @GET("api/locations/{id}")
    suspend fun getLocation(@Path("id") id: UUID): Location

    /**
     * Delete a Location
     */
    @DELETE("api/locations/{id}")
    suspend fun deleteLocation(@Path("id") id: UUID)

    /**
     * Update a Location
     */
    @POST("api/locations/{id}")
    suspend fun updateLocation(@Path("id") id: UUID, @Body location: LocationCreate): Location
}