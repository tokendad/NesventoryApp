package com.example.nesventorynew.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.UUID

// Data model for the Token response
data class LoginResponse(
    val access_token: String,
    val token_type: String
)

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
}