package com.tokendad.nesventory.ui.server

import com.tokendad.nesventory.data.remote.CsvImportResult
import com.tokendad.nesventory.data.remote.NetworkDiscoveredItem
import com.tokendad.nesventory.data.remote.NetworkScanResult
import com.tokendad.nesventory.data.repository.ImportRepository
import com.tokendad.nesventory.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val importRepository = mockk<ImportRepository>(relaxed = true)

    @Test
    fun `scanNetwork loads discovered items`() = runTest {
        coEvery { importRepository.scanNetwork() } returns NetworkScanResult(
            discovered_items = listOf(NetworkDiscoveredItem(id = "a1", name = "TV")),
            scan_duration_ms = 1500
        )
        val viewModel = ImportViewModel(importRepository)

        viewModel.scanNetwork()
        advanceUntilIdle()

        assertEquals(1, viewModel.networkItems.size)
        assertEquals("a1", viewModel.networkItems.first().id)
    }

    @Test
    fun `importSelectedNetworkItems requires selection`() = runTest {
        val viewModel = ImportViewModel(importRepository)

        viewModel.importSelectedNetworkItems()

        assertEquals("Select at least one discovered item", viewModel.errorMessage)
        coVerify(exactly = 0) { importRepository.importNetworkItems(any()) }
    }

    @Test
    fun `importSelectedNetworkItems delegates with selected ids`() = runTest {
        coEvery { importRepository.importNetworkItems(listOf("x1")) } returns CsvImportResult(
            imported_count = 1,
            failed_count = 0
        )
        val viewModel = ImportViewModel(importRepository)
        viewModel.selectedNetworkItemIds = setOf("x1")

        viewModel.importSelectedNetworkItems()
        advanceUntilIdle()

        assertEquals(1, viewModel.csvResult?.imported_count)
        coVerify(exactly = 1) { importRepository.importNetworkItems(listOf("x1")) }
    }
}
