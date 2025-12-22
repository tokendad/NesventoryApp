package com.example.nesventorynew.data.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

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
     * Get Item Details
     */

    @GET("api/items/")
    suspend fun getItems(): List<Item>
}