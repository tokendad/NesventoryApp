package com.nesventory.android.data.api

import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Location
import com.nesventory.android.data.model.TokenResponse
import com.nesventory.android.data.model.User
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
}
