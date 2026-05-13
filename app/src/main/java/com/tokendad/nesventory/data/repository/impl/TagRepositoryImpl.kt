package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.Tag
import com.tokendad.nesventory.data.remote.TagCreate
import com.tokendad.nesventory.data.repository.TagRepository
import java.util.UUID
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : TagRepository {
    override suspend fun getTags(): List<Tag> = api.getTags()

    override suspend fun createTag(tag: TagCreate): Tag = api.createTag(tag)

    override suspend fun deleteTag(id: UUID) {
        val response = api.deleteTag(id)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to delete tag (HTTP ${response.code()})")
        }
    }
}
