package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.AIStatusResponse
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SystemRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = SystemRepositoryImpl(api)

    @Test
    fun `getAIStatus delegates to api`() = runTest {
        val expected = AIStatusResponse(enabled = true, model = "test-model")
        coEvery { api.getAIStatus() } returns expected

        val actual = repository.getAIStatus()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getAIStatus() }
    }

    @Test
    fun `testAIConnection propagates api exceptions`() = runTest {
        coEvery { api.testAIConnection() } throws IllegalStateException("boom")

        try {
            repository.testAIConnection()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }
    }
}
