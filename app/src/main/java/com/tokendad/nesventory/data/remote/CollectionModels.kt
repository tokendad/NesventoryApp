package com.tokendad.nesventory.data.remote

import java.util.UUID

data class Collection(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val parent_id: UUID? = null,
    val cover_image_path: String? = null,
    val item_count: Int = 0,
    val total_item_count: Int = 0,
    val children: List<Collection> = emptyList(),
    val shared_properties: Map<String, Any>? = null,
    val created_at: String,
    val updated_at: String
)

data class CollectionCreate(
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val parent_id: UUID? = null,
    val shared_properties: Map<String, Any>? = null
)

data class CollectionUpdate(
    val name: String? = null,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val parent_id: UUID? = null,
    val shared_properties: Map<String, Any>? = null
)

data class AddItemsToCollectionRequest(
    val item_ids: List<UUID>
)
