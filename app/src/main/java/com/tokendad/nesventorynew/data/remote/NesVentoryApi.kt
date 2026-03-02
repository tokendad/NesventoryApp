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
     * Root /token fallback for mobile compatibility with upstream auth behavior.
     * Some server versions use root /token instead of /api/token.
     */
    @FormUrlEncoded
    @POST("token")
    suspend fun loginFallback(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    /**
     * Google OAuth authentication endpoint.
     * Exchanges Google ID token for NesVentory access token.
     *
     * Returns [retrofit2.Response] so callers can inspect HTTP status, headers
     * (especially Set-Cookie) and the parsed body independently.
     */
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequest): retrofit2.Response<GoogleAuthResponse>

    /**
     * Check if Google OAuth is enabled on the server.
     * Returns the client ID needed for Android Credential Manager.
     */
    @GET("api/auth/google/status")
    suspend fun getGoogleAuthStatus(): GoogleAuthStatus

    /**
     * Get system status including health and version.
     */
    @GET("api/status")
    suspend fun getStatus(): StatusResponse

    /**
     * Get media statistics (Total counts, etc.)
     */
    @GET("api/media/stats")
    suspend fun getMediaStats(): MediaStatsResponse
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
    suspend fun detectItems(
        @Part file: MultipartBody.Part,
        @Part("use_plugins") usePlugins: Boolean = true
    ): DetectionResult

    /**
     * Check AI Status
     */
    @GET("api/ai/status")
    suspend fun getAIStatus(): AIStatusResponse

    /**
     * Test AI Connection
     */
    @POST("api/ai/test-connection")
    suspend fun testAIConnection(): AITestConnectionResponse

    /**
     * Parse Data Tag from Image
     */
    @Multipart
    @POST("api/ai/parse-data-tag")
    suspend fun parseDataTag(
        @Part file: MultipartBody.Part,
        @Part("use_plugins") usePlugins: Boolean = true
    ): DataTagInfo

    /**
     * Scan Barcode from Image
     */
    @Multipart
    @POST("api/ai/scan-barcode")
    suspend fun scanBarcode(
        @Part file: MultipartBody.Part
    ): BarcodeScanResult

    /**
     * Lookup Barcode Information
     */
    @POST("api/ai/barcode-lookup")
    suspend fun lookupBarcode(
        @Body request: BarcodeLookupRequest
    ): BarcodeLookupResult

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
    @PUT("api/items/{id}")
    suspend fun updateItem(@Path("id") id: UUID, @Body item: ItemCreate): Item

    /**
     * Enrich Item details via AI
     */
    @POST("api/items/{id}/enrich")
    suspend fun enrichItem(@Path("id") id: UUID): ItemEnrichmentResult

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
     * Printer Management
     */
    @GET("api/printer/config")
    suspend fun getPrinterConfig(): PrinterConfig

    @PUT("api/printer/config")
    suspend fun updatePrinterConfig(@Body config: PrinterConfig): PrinterConfig

    @GET("api/printer/models")
    suspend fun getPrinterModels(): PrinterModelsResponse

    @POST("api/printer/print-label")
    suspend fun printLabel(@Body request: PrintJobRequest): PrintLabelResponse

    @GET("api/printer/status")
    suspend fun getPrinterStatus(): PrinterStatus

    @POST("api/printer/test-connection")
    suspend fun testPrinterConnection(@Body config: PrinterConfig): PrinterTestResult

    @POST("api/printer/print-test-label")
    suspend fun printTestLabel(): PrintLabelResponse

    /**
     * Printer Profile Management
     */
    @GET("api/printer/profiles")
    suspend fun getPrinterProfiles(): PrinterProfilesResponse

    @POST("api/printer/profiles")
    suspend fun createPrinterProfile(@Body profile: PrinterProfile): PrinterProfile

    @DELETE("api/printer/profiles/{profileId}")
    suspend fun deletePrinterProfile(@Path("profileId") profileId: String)

    @GET("api/printer/config/active")
    suspend fun getActivePrinterConfig(): PrinterConfig

    @POST("api/printer/config/activate/{profileId}")
    suspend fun activatePrinterProfile(@Path("profileId") profileId: String): PrinterConfig

    /**
     * System Printer Management (CUPS)
     */
    @GET("api/printer/system/list")
    suspend fun getSystemPrinters(): SystemPrintersResponse

    @POST("api/printer/system/set-default")
    suspend fun setDefaultSystemPrinter(@Body request: Map<String, String>): Map<String, Any>

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
     * Get Location Categories
     */
    @GET("api/settings/location-categories")
    suspend fun getLocationCategories(): List<String>

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
    @PUT("api/locations/{id}")
    suspend fun updateLocation(@Path("id") id: UUID, @Body location: LocationCreate): Location
}