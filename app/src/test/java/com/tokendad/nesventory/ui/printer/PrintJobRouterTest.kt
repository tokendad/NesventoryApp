package com.tokendad.nesventory.ui.printer

import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.ServerSettings
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.PrintLabelResponse
import com.tokendad.nesventory.data.repository.PrinterRepository
import com.tokendad.nesventory.util.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PrintJobRouterTest {
    private val preferencesManager = mockk<PreferencesManager>()
    private val printerRepository = mockk<PrinterRepository>()
    private val printJobExecutor = mockk<PrintJobExecutor>()

    private val router = PrintJobRouter(
        preferencesManager = preferencesManager,
        printerRepository = printerRepository,
        printJobExecutor = printJobExecutor
    )

    @Test
    fun `printItem returns success when server print succeeds`() = runTest {
        every { preferencesManager.serverSettings } returns flowOf(
            ServerSettings(printMethod = "server")
        )
        coEvery { printerRepository.printLabel(any()) } returns PrintLabelResponse(
            success = true,
            message = "Queued"
        )

        val item = createItem()
        val result = router.printItem(item)

        assertTrue(result is PrintResult.Success)
        assertEquals("Queued", (result as PrintResult.Success).message)
        coVerify {
            printerRepository.printLabel(
                match { it.entity_id == item.id && it.entity_type == "item" && it.quantity == 1 }
            )
        }
    }

    @Test
    fun `printLocation returns error when server reports failure`() = runTest {
        every { preferencesManager.serverSettings } returns flowOf(
            ServerSettings(printMethod = "server")
        )
        coEvery { printerRepository.printLabel(any()) } returns PrintLabelResponse(
            success = false,
            message = "Printer offline"
        )

        val result = router.printLocation(createLocation())

        assertTrue(result is PrintResult.Error)
        assertEquals("Printer offline", (result as PrintResult.Error).message)
    }

    @Test
    fun `printItem returns error when local printer disconnected`() = runTest {
        every { preferencesManager.serverSettings } returns flowOf(
            ServerSettings(printMethod = "local")
        )
        every { printJobExecutor.isConnected() } returns false

        val result = router.printItem(createItem())

        assertTrue(result is PrintResult.Error)
        assertEquals("Printer not connected. Go to Printer Settings.", (result as PrintResult.Error).message)
        coVerify(exactly = 0) {
            printJobExecutor.printLabel(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `printLocation local path normalizes defaults and density`() = runTest {
        every { preferencesManager.serverSettings } returns flowOf(
            ServerSettings(
                remoteUrl = "   ",
                printMethod = "local",
                localPrinterModel = "UNKNOWN",
                localPrinterDensity = 99
            )
        )
        every { printJobExecutor.isConnected() } returns true
        coEvery {
            printJobExecutor.printLabel(any(), any(), any(), any(), any(), any())
        } returns Unit

        val location = createLocation()
        val result = router.printLocation(location)

        assertTrue(result is PrintResult.Success)
        coVerify {
            printJobExecutor.printLabel(
                labelText = location.name,
                labelSubtitle = location.id.toString().take(8),
                qrContent = "${Constants.DEFAULT_REMOTE_URL}/api/locations/${location.id}",
                iconType = "location",
                model = PrinterModel.D11_H,
                density = 5
            )
        }
    }

    private fun createItem(): Item {
        return Item(
            id = UUID.randomUUID(),
            name = "Hammer",
            created_at = "2024-01-01T00:00:00Z",
            updated_at = "2024-01-01T00:00:00Z"
        )
    }

    private fun createLocation(): Location {
        return Location(
            id = UUID.randomUUID(),
            name = "Garage",
            created_at = "2024-01-01T00:00:00Z",
            updated_at = "2024-01-01T00:00:00Z"
        )
    }
}
