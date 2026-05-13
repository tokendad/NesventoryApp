package com.tokendad.nesventory.data.remote

import java.util.UUID

data class Tag(
    val id: UUID,
    val name: String,
    val color: String? = null,
    val created_at: String
)

data class TagCreate(
    val name: String,
    val color: String? = null
)
