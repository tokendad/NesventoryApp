package com.tokendad.nesventory.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
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
    suspend fun getItems(
        @Query("search") search: String? = null,
        @Query("location_id") locationId: UUID? = null,
        @Query("is_living") isLiving: Boolean? = null,
        @Query("relationship_type") relationshipType: String? = null,
        @Query("collection_id") collectionId: UUID? = null,
        @Query("collection_id_recursive") collectionIdRecursive: Boolean? = null
    ): List<Item>

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

    @POST("api/items/bulk-delete")
    suspend fun bulkDeleteItems(@Body request: BulkDeleteRequest): BulkOperationResponse

    @POST("api/items/bulk-update-tags")
    suspend fun bulkUpdateItemTags(@Body request: BulkUpdateTagsRequest): BulkOperationResponse

    @POST("api/items/bulk-update-location")
    suspend fun bulkUpdateItemLocation(@Body request: BulkUpdateLocationRequest): BulkOperationResponse

    /**
     * Update an Item
     */
    @PUT("api/items/{id}")
    suspend fun updateItem(@Path("id") id: UUID, @Body item: ItemUpdate): Item

    @GET("api/tags/")
    suspend fun getTags(): List<Tag>

    @POST("api/tags/")
    suspend fun createTag(@Body tag: TagCreate): Tag

    @DELETE("api/tags/{id}")
    suspend fun deleteTag(@Path("id") id: UUID): Response<Unit>

    @GET("api/gdrive/status")
    suspend fun getGDriveStatus(): GDriveStatus

    @POST("api/gdrive/connect")
    suspend fun connectGDrive(): GDriveConnectResponse

    @DELETE("api/gdrive/disconnect")
    suspend fun disconnectGDrive(): Response<Unit>

    @POST("api/gdrive/backup")
    suspend fun triggerGDriveBackup(): GDriveBackupResult

    @GET("api/gdrive/backups")
    suspend fun listGDriveBackups(): List<GDriveBackup>

    @DELETE("api/gdrive/backups/{backup_id}")
    suspend fun deleteGDriveBackup(@Path("backup_id") backupId: String): Response<Unit>

    @GET("api/users/me")
    suspend fun getMyProfile(): UserProfile

    @PATCH("api/users/{user_id}")
    suspend fun updateProfile(
        @Path("user_id") userId: UUID,
        @Body update: UserProfileUpdate
    ): UserProfile

    @POST("api/users/me/set-password")
    suspend fun setPassword(@Body request: SetPasswordRequest): StatusResponse

    /**
     * Enrich Item details via AI
     */
    @POST("api/items/{id}/enrich")
    suspend fun enrichItem(@Path("id") id: UUID): ItemEnrichmentResult

    @GET("api/items/{id}/collections")
    suspend fun getItemCollections(@Path("id") itemId: UUID): List<Collection>

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
    suspend fun setDefaultSystemPrinter(@Body request: Map<String, String>): StatusResponse

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

    @Multipart
    @POST("api/locations/{location_id}/photos")
    suspend fun uploadLocationPhoto(
        @Path("location_id") locationId: UUID,
        @Part file: MultipartBody.Part,
        @Part("is_primary") isPrimary: Boolean = false,
        @Part("photo_type") photoType: String? = null
    ): LocationPhoto

    @DELETE("api/locations/{location_id}/photos/{photo_id}")
    suspend fun deleteLocationPhoto(
        @Path("location_id") locationId: UUID,
        @Path("photo_id") photoId: UUID
    ): Response<Unit>

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
    suspend fun updateLocation(@Path("id") id: UUID, @Body location: LocationUpdate): Location

    @GET("api/collections/")
    suspend fun getCollections(): List<Collection>

    @POST("api/collections/")
    suspend fun createCollection(@Body request: CollectionCreate): Collection

    @GET("api/collections/tree")
    suspend fun getCollectionsTree(): List<Collection>

    @GET("api/collections/{id}")
    suspend fun getCollection(@Path("id") id: UUID): Collection

    @PUT("api/collections/{id}")
    suspend fun updateCollection(@Path("id") id: UUID, @Body request: CollectionUpdate): Collection

    @DELETE("api/collections/{id}")
    suspend fun deleteCollection(@Path("id") id: UUID): Response<Unit>

    @GET("api/collections/{id}/items")
    suspend fun getCollectionItems(@Path("id") id: UUID): List<Item>

    @POST("api/collections/{id}/items")
    suspend fun addItemsToCollection(
        @Path("id") collectionId: UUID,
        @Body request: AddItemsToCollectionRequest
    ): StatusResponse

    @DELETE("api/collections/{id}/items/{item_id}")
    suspend fun removeItemFromCollection(
        @Path("id") collectionId: UUID,
        @Path("item_id") itemId: UUID
    ): Response<Unit>

    @GET("api/collections/{id}/children")
    suspend fun getCollectionChildren(@Path("id") id: UUID): List<Collection>

    @Multipart
    @POST("api/collections/{id}/cover-image")
    suspend fun uploadCollectionCoverImage(
        @Path("id") collectionId: UUID,
        @Part file: MultipartBody.Part
    ): Collection
}