package com.tokendad.nesventory.ui.edititem

import androidx.lifecycle.SavedStateHandle
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.ServerSettings
import com.tokendad.nesventory.data.remote.EnrichedItemData
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.ItemEnrichmentResult
import com.tokendad.nesventory.data.remote.ItemUpdate
import com.tokendad.nesventory.data.repository.ItemRepository
import com.tokendad.nesventory.data.repository.LocationRepository
import com.tokendad.nesventory.data.repository.MaintenanceRepository
import com.tokendad.nesventory.data.repository.TagRepository
import com.tokendad.nesventory.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class EditItemViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val itemRepository = mockk<ItemRepository>(relaxed = true)
    private val locationRepository = mockk<LocationRepository>(relaxed = true)
    private val maintenanceRepository = mockk<MaintenanceRepository>(relaxed = true)
    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>()

    private lateinit var viewModel: EditItemViewModel

    @Before
    fun setup() {
        every { preferencesManager.serverSettings } returns flowOf(
            ServerSettings(remoteUrl = "https://example.com")
        )
        coEvery { locationRepository.getLocations() } returns emptyList()
        coEvery { tagRepository.getTags() } returns emptyList()
        viewModel = EditItemViewModel(
            itemRepository = itemRepository,
            locationRepository = locationRepository,
            maintenanceRepository = maintenanceRepository,
            tagRepository = tagRepository,
            preferencesManager = preferencesManager,
            savedStateHandle = SavedStateHandle()
        )
    }

    @Test
    fun `enrichData applies changes and discard restores originals`() = runTest {
        val itemId = UUID.randomUUID()
        viewModel.itemId = itemId
        viewModel.description = "Original description"
        viewModel.brand = "Original brand"
        viewModel.modelNumber = "M1"
        viewModel.serialNumber = "S1"
        viewModel.estimatedValue = "10.00"

        coEvery { itemRepository.enrichItem(itemId) } returns ItemEnrichmentResult(
            item_id = itemId,
            enriched_data = listOf(
                EnrichedItemData(
                    description = "New description",
                    brand = "New brand",
                    model_number = "M2",
                    serial_number = "S2",
                    estimated_value = "20.00",
                    source = "ai"
                )
            ),
            message = "ok"
        )

        viewModel.enrichData()
        advanceUntilIdle()

        assertTrue(viewModel.isReviewingEnrichment)
        assertEquals("New description", viewModel.description)
        assertTrue(viewModel.isFieldModified("description", viewModel.description))

        viewModel.discardEnrichment()

        assertFalse(viewModel.isReviewingEnrichment)
        assertEquals("Original description", viewModel.description)
        assertEquals("Original brand", viewModel.brand)
        assertEquals("M1", viewModel.modelNumber)
        assertEquals("S1", viewModel.serialNumber)
        assertEquals("10.00", viewModel.estimatedValue)
    }

    @Test
    fun `updateItem sends expected payload`() = runTest {
        val itemId = UUID.randomUUID()
        viewModel.itemId = itemId
        viewModel.name = "Desk Lamp"
        viewModel.description = "Warm light"
        viewModel.brand = "NesBrand"
        viewModel.modelNumber = "LB-12"
        viewModel.serialNumber = "SER-1"
        viewModel.purchasePrice = "49.99"
        viewModel.purchaseDate = "2025-01-01"
        viewModel.estimatedValue = "35.00"
        viewModel.retailer = "Store"
        viewModel.isLiving = false
        val tagId = UUID.randomUUID()
        viewModel.addTag(tagId)

        val payloadSlot = slot<ItemUpdate>()
        coEvery { itemRepository.updateItem(itemId, capture(payloadSlot)) } returns Item(
            id = itemId,
            name = "Desk Lamp",
            created_at = "2025-01-01T00:00:00Z",
            updated_at = "2025-01-01T00:00:00Z"
        )

        var successCalled = false
        viewModel.updateItem { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        val payload = payloadSlot.captured
        assertEquals("Desk Lamp", payload.name)
        assertEquals("Warm light", payload.description)
        assertEquals("NesBrand", payload.brand)
        assertEquals("LB-12", payload.model_number)
        assertEquals("SER-1", payload.serial_number)
        assertEquals("49.99", payload.purchase_price)
        assertEquals("2025-01-01", payload.purchase_date)
        assertEquals("35.00", payload.estimated_value)
        assertEquals("Store", payload.retailer)
        assertEquals(false, payload.is_living)
        assertEquals(listOf(tagId), payload.tag_ids)
        coVerify(exactly = 1) { itemRepository.updateItem(itemId, any()) }
    }
}
