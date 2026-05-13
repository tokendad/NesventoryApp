package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.Tag
import com.tokendad.nesventory.data.remote.TagCreate
import java.util.UUID

interface TagRepository {
    suspend fun getTags(): List<Tag>
    suspend fun createTag(tag: TagCreate): Tag
    suspend fun deleteTag(id: UUID)
}
