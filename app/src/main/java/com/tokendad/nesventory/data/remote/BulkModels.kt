package com.tokendad.nesventory.data.remote

import java.util.UUID

data class BulkDeleteRequest(
    val item_ids: List<UUID>
)

data class BulkUpdateTagsRequest(
    val item_ids: List<UUID>,
    val tag_ids: List<UUID>,
    val action: String
)

data class BulkUpdateLocationRequest(
    val item_ids: List<UUID>,
    val location_id: UUID?
)

data class BulkOperationResponse(
    val success_count: Int,
    val failed_count: Int,
    val errors: List<String> = emptyList()
)
