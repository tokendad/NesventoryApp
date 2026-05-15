package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.AddItemsToCollectionRequest
import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.CollectionCreate
import com.tokendad.nesventory.data.remote.CollectionUpdate
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.repository.CollectionRepository
import com.tokendad.nesventory.data.repository.ItemRepository
import okhttp3.MultipartBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi,
    private val itemRepository: ItemRepository
) : CollectionRepository {
    override suspend fun getCollections(): List<Collection> = api.getCollections()

    override suspend fun getCollectionsTree(): List<Collection> = api.getCollectionsTree()

    override suspend fun getCollection(id: UUID): Collection = api.getCollection(id)

    override suspend fun createCollection(request: CollectionCreate): Collection = api.createCollection(request)

    override suspend fun updateCollection(id: UUID, request: CollectionUpdate): Collection =
        api.updateCollection(id, request)

    override suspend fun deleteCollection(id: UUID) {
        api.deleteCollection(id)
    }

    // Uses GET /api/items/?collection_id={id} (plain array) instead of
    // GET /api/collections/{id}/items which returns a paginated object.
    override suspend fun getCollectionItems(id: UUID): List<Item> =
        itemRepository.getItems(collectionId = id)

    override suspend fun addItemsToCollection(collectionId: UUID, itemIds: List<UUID>) {
        api.addItemsToCollection(collectionId, AddItemsToCollectionRequest(item_ids = itemIds))
    }

    override suspend fun removeItemFromCollection(collectionId: UUID, itemId: UUID) {
        api.removeItemFromCollection(collectionId, itemId)
    }

    override suspend fun getCollectionChildren(id: UUID): List<Collection> = api.getCollectionChildren(id)

    override suspend fun getItemCollections(itemId: UUID): List<Collection> = api.getItemCollections(itemId)

    override suspend fun uploadCollectionCoverImage(collectionId: UUID, file: MultipartBody.Part): Collection =
        api.uploadCollectionCoverImage(collectionId, file)
}
