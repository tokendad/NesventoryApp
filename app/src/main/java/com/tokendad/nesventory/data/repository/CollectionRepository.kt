package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.CollectionCreate
import com.tokendad.nesventory.data.remote.CollectionUpdate
import com.tokendad.nesventory.data.remote.Item
import okhttp3.MultipartBody
import java.util.UUID

interface CollectionRepository {
    suspend fun getCollections(): List<Collection>
    suspend fun getCollectionsTree(): List<Collection>
    suspend fun getCollection(id: UUID): Collection
    suspend fun createCollection(request: CollectionCreate): Collection
    suspend fun updateCollection(id: UUID, request: CollectionUpdate): Collection
    suspend fun deleteCollection(id: UUID)
    suspend fun getCollectionItems(id: UUID): List<Item>
    suspend fun addItemsToCollection(collectionId: UUID, itemIds: List<UUID>)
    suspend fun removeItemFromCollection(collectionId: UUID, itemId: UUID)
    suspend fun getCollectionChildren(id: UUID): List<Collection>
    suspend fun getItemCollections(itemId: UUID): List<Collection>
    suspend fun uploadCollectionCoverImage(collectionId: UUID, file: MultipartBody.Part): Collection
}
