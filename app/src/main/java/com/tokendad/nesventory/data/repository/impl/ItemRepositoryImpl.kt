package com.tokendad.nesventory.data.repository.impl

import com.google.gson.Gson
import com.tokendad.nesventory.data.remote.BarcodeLookupRequest
import com.tokendad.nesventory.data.remote.BarcodeLookupResult
import com.tokendad.nesventory.data.remote.BarcodeScanResult
import com.tokendad.nesventory.data.remote.BulkDeleteRequest
import com.tokendad.nesventory.data.remote.BulkOperationResponse
import com.tokendad.nesventory.data.remote.BulkUpdateLocationRequest
import com.tokendad.nesventory.data.remote.BulkUpdateTagsRequest
import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.DataTagInfo
import com.tokendad.nesventory.data.remote.DetectionResult
import com.tokendad.nesventory.data.remote.Document
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.ItemCreate
import com.tokendad.nesventory.data.remote.ItemEnrichmentResult
import com.tokendad.nesventory.data.remote.ItemUpdate
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.Photo
import com.tokendad.nesventory.data.local.ItemDao
import com.tokendad.nesventory.data.local.ItemEntity
import com.tokendad.nesventory.data.local.SyncEntityType
import com.tokendad.nesventory.data.local.SyncOperation
import com.tokendad.nesventory.data.local.SyncQueueDao
import com.tokendad.nesventory.data.local.SyncQueueEntity
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.repository.ItemRepository
import okhttp3.MultipartBody
import retrofit2.HttpException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi,
    private val itemDao: ItemDao,
    private val syncQueueDao: SyncQueueDao,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) : ItemRepository {

    private data class DeleteSyncPayload(val profileId: String? = null)
    @Volatile private var activeCacheProfileId: String? = null
    private val cacheMutex = Mutex()

    private fun Item.toEntity(): ItemEntity = ItemEntity(
        id = id.toString(),
        name = name,
        payload = gson.toJson(this),
        updatedAtMillis = System.currentTimeMillis()
    )

    private fun ItemEntity.toModel(): Item = gson.fromJson(payload, Item::class.java)

    override suspend fun getItems(
        search: String?,
        locationId: UUID?,
        isLiving: Boolean?,
        relationshipType: String?,
        collectionId: UUID?,
        collectionIdRecursive: Boolean?,
        page: Int?,
        limit: Int?
    ): List<Item> {
        ensureProfileScopedCache()
        val cached = itemDao.getAll()
            .map { it.toModel() }
            .sortedBy { it.id.toString() }
        val pageNumber = page ?: 1
        val pageSize = limit?.takeIf { it > 0 }
        val isCanonicalCacheQuery = search.isNullOrBlank() &&
            locationId == null &&
            isLiving == null &&
            relationshipType.isNullOrBlank() &&
            collectionId == null &&
            collectionIdRecursive == null &&
            page == null &&
            limit == null

        return try {
            val remote = api.getItems(
                search = search,
                locationId = locationId,
                isLiving = isLiving,
                relationshipType = relationshipType,
                collectionId = collectionId,
                collectionIdRecursive = collectionIdRecursive,
                page = page,
                limit = limit
            )
            if (isCanonicalCacheQuery) {
                if (pageNumber <= 1) {
                    itemDao.clearAll()
                }
                itemDao.upsertAll(remote.map { it.toEntity() })
            }
            remote
        } catch (error: Exception) {
            if (cached.isEmpty()) throw error
            val filtered = applyFilters(cached, search, locationId, isLiving, relationshipType, collectionId)
            sliceForPage(filtered, pageNumber, pageSize)
        }
    }

    override suspend fun getItem(id: UUID): Item {
        ensureProfileScopedCache()
        return try {
            val remote = api.getItem(id)
            itemDao.upsert(remote.toEntity())
            remote
        } catch (error: Exception) {
            itemDao.getById(id.toString())?.toModel() ?: throw error
        }
    }

    override suspend fun createItem(item: ItemCreate): Item {
        ensureProfileScopedCache()
        val created = api.createItem(item)
        itemDao.upsert(created.toEntity())
        return created
    }

    override suspend fun updateItem(id: UUID, item: ItemUpdate): Item {
        ensureProfileScopedCache()
        val updated = api.updateItem(id, item)
        itemDao.upsert(updated.toEntity())
        return updated
    }

    override suspend fun deleteItem(id: UUID) {
        ensureProfileScopedCache()
        try {
            api.deleteItem(id)
            itemDao.deleteById(id.toString())
        } catch (error: Exception) {
            val shouldQueueDelete = error is IOException ||
                (error is HttpException && error.code() >= 500)
            if (!shouldQueueDelete) throw error
            syncQueueDao.insert(
                SyncQueueEntity(
                    operation = SyncOperation.DELETE,
                    entityType = SyncEntityType.ITEM,
                    entityId = id.toString(),
                    payload = gson.toJson(DeleteSyncPayload(activeProfileId()))
                )
            )
            itemDao.deleteById(id.toString())
        }
    }

    override suspend fun bulkDeleteItems(request: BulkDeleteRequest): BulkOperationResponse =
        api.bulkDeleteItems(request)

    override suspend fun bulkUpdateItemTags(request: BulkUpdateTagsRequest): BulkOperationResponse =
        api.bulkUpdateItemTags(request)

    override suspend fun bulkUpdateItemLocation(request: BulkUpdateLocationRequest): BulkOperationResponse =
        api.bulkUpdateItemLocation(request)

    override suspend fun getItemCollections(itemId: UUID): List<Collection> = api.getItemCollections(itemId)

    override suspend fun enrichItem(id: UUID): ItemEnrichmentResult = api.enrichItem(id)

    override suspend fun detectItems(file: MultipartBody.Part, usePlugins: Boolean): DetectionResult =
        api.detectItems(file, usePlugins)

    override suspend fun scanBarcode(file: MultipartBody.Part): BarcodeScanResult = api.scanBarcode(file)

    override suspend fun lookupBarcode(request: BarcodeLookupRequest): BarcodeLookupResult =
        api.lookupBarcode(request)

    override suspend fun parseDataTag(file: MultipartBody.Part, usePlugins: Boolean): DataTagInfo =
        api.parseDataTag(file, usePlugins)

    override suspend fun uploadItemPhoto(
        itemId: UUID,
        file: MultipartBody.Part,
        isPrimary: Boolean,
        isDataTag: Boolean,
        photoType: String?
    ): Photo = api.uploadItemPhoto(itemId, file, isPrimary, isDataTag, photoType)

    override suspend fun deleteItemPhoto(itemId: UUID, photoId: UUID) =
        api.deleteItemPhoto(itemId, photoId)

    override suspend fun uploadItemDocument(
        itemId: UUID,
        file: MultipartBody.Part,
        documentType: String?
    ): Document = api.uploadItemDocument(itemId, file, documentType)

    override suspend fun deleteItemDocument(itemId: UUID, documentId: UUID) =
        api.deleteItemDocument(itemId, documentId)

    private fun applyFilters(
        items: List<Item>,
        search: String?,
        locationId: UUID?,
        isLiving: Boolean?,
        relationshipType: String?,
        collectionId: UUID?
    ): List<Item> {
        return items.filter { item ->
            val matchesSearch = search.isNullOrBlank() || item.name.contains(search, true) ||
                (item.brand?.contains(search, true) == true)
            val matchesLocation = locationId == null || item.location_id == locationId
            val matchesLiving = isLiving == null || item.is_living == isLiving
            val matchesRelationship = relationshipType.isNullOrBlank() ||
                item.relationship_type.equals(relationshipType, true)
            // Collection membership cannot be derived from local cache safely.
            val matchesCollection = collectionId == null
            matchesSearch && matchesLocation && matchesLiving && matchesRelationship && matchesCollection
        }
    }

    private fun sliceForPage(items: List<Item>, page: Int, limit: Int?): List<Item> {
        val pageSize = limit ?: return items
        val safePage = page.coerceAtLeast(1)
        val fromIndex = ((safePage - 1) * pageSize).coerceAtMost(items.size)
        val toIndex = (fromIndex + pageSize).coerceAtMost(items.size)
        return items.subList(fromIndex, toIndex)
    }

    private suspend fun activeProfileId(): String? {
        return preferencesManager.serverProfiles.first().activeProfileId
    }

    private suspend fun ensureProfileScopedCache() {
        val profileId = activeProfileId()
        cacheMutex.withLock {
            if (profileId != activeCacheProfileId) {
                itemDao.clearAll()
                activeCacheProfileId = profileId
            }
        }
    }
}
