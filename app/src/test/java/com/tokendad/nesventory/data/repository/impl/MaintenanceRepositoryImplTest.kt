package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.MaintenanceTask
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class MaintenanceRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = MaintenanceRepositoryImpl(api)

    @Test
    fun `getMaintenanceTasksForItem delegates to api`() = runTest {
        val itemId = UUID.randomUUID()
        val expected = listOf(mockk<MaintenanceTask>(relaxed = true))
        coEvery { api.getMaintenanceTasksForItem(itemId) } returns expected

        val actual = repository.getMaintenanceTasksForItem(itemId)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getMaintenanceTasksForItem(itemId) }
    }

    @Test
    fun `getMaintenanceTask propagates api exceptions`() = runTest {
        val taskId = UUID.randomUUID()
        coEvery { api.getMaintenanceTask(taskId) } throws IllegalStateException("boom")

        try {
            repository.getMaintenanceTask(taskId)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }
    }
}
