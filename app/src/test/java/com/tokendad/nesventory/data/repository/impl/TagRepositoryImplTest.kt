package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.Tag
import com.tokendad.nesventory.data.remote.TagCreate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response
import java.util.UUID

class TagRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = TagRepositoryImpl(api)

    @Test
    fun `getTags delegates to api`() = runTest {
        val expected = listOf(mockk<Tag>(relaxed = true))
        coEvery { api.getTags() } returns expected

        val actual = repository.getTags()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getTags() }
    }

    @Test
    fun `createTag delegates to api`() = runTest {
        val request = TagCreate(name = "Important", color = "#FF0000")
        val expected = mockk<Tag>(relaxed = true)
        coEvery { api.createTag(request) } returns expected

        val actual = repository.createTag(request)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.createTag(request) }
    }

    @Test
    fun `deleteTag throws when api response is unsuccessful`() = runTest {
        val id = UUID.randomUUID()
        val responseBody = "failed".toResponseBody("text/plain".toMediaType())
        coEvery { api.deleteTag(id) } returns Response.error(500, responseBody)

        try {
            repository.deleteTag(id)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("Failed to delete tag (HTTP 500)", e.message)
        }
    }
}
