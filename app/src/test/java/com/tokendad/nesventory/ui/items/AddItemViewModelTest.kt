package com.tokendad.nesventory.ui.items

import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.repository.ItemRepository
import com.tokendad.nesventory.data.repository.LocationRepository
import com.tokendad.nesventory.testutil.MainDispatcherRule
import com.tokendad.nesventory.ui.additem.AddItemViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AddItemViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val itemRepository = mockk<ItemRepository>(relaxed = true)
    private val locationRepository = mockk<LocationRepository>()

    private lateinit var viewModel: AddItemViewModel

    @Before
    fun setup() {
        coEvery { locationRepository.getLocations() } returns emptyList()
        viewModel = AddItemViewModel(itemRepository, locationRepository)
    }

    @Test
    fun `createItem sets error when name is blank`() = runTest {
        viewModel.name = ""

        viewModel.createItem {}

        assertEquals("Name is required", viewModel.errorMessage)
    }

    @Test
    fun `onLivingChanged false clears living-only fields`() = runTest {
        viewModel.isLiving = true
        viewModel.relationshipType = "pet"
        viewModel.birthdate = "2020-01-01"
        viewModel.contactPhone = "123"
        viewModel.contactEmail = "a@b.com"
        viewModel.contactNotes = "note"

        viewModel.onLivingChanged(false)

        assertEquals(false, viewModel.isLiving)
        assertEquals("person", viewModel.relationshipType)
        assertEquals("", viewModel.birthdate)
        assertEquals("", viewModel.contactPhone)
        assertEquals("", viewModel.contactEmail)
        assertEquals("", viewModel.contactNotes)
    }

    @Test
    fun `living item requires home location`() = runTest {
        val nonHomeId = UUID.randomUUID()
        val homeLocation = Location(
            id = UUID.randomUUID(),
            name = "Home Base",
            created_at = "2024-01-01T00:00:00Z",
            updated_at = "2024-01-01T00:00:00Z",
            location_category = "Home"
        )
        coEvery { locationRepository.getLocations() } returns listOf(
            Location(
                id = nonHomeId,
                name = "Garage",
                created_at = "2024-01-01T00:00:00Z",
                updated_at = "2024-01-01T00:00:00Z",
                location_category = "Storage"
            ),
            homeLocation
        )
        viewModel = AddItemViewModel(itemRepository, locationRepository)
        advanceUntilIdle()

        viewModel.name = "Buddy"
        viewModel.isLiving = true
        viewModel.selectedLocationId = nonHomeId

        viewModel.createItem {}

        assertEquals("Living items must be assigned to a Home location", viewModel.errorMessage)
    }

    @Test
    fun `onLivingChanged true assigns first home location when needed`() = runTest {
        val homeId = UUID.randomUUID()
        coEvery { locationRepository.getLocations() } returns listOf(
            Location(
                id = UUID.randomUUID(),
                name = "Office",
                created_at = "2024-01-01T00:00:00Z",
                updated_at = "2024-01-01T00:00:00Z",
                location_category = "Work"
            ),
            Location(
                id = homeId,
                name = "Home",
                created_at = "2024-01-01T00:00:00Z",
                updated_at = "2024-01-01T00:00:00Z",
                location_category = "Home"
            )
        )
        viewModel = AddItemViewModel(itemRepository, locationRepository)
        advanceUntilIdle()

        viewModel.selectedLocationId = UUID.randomUUID()
        viewModel.onLivingChanged(true)

        assertEquals(homeId, viewModel.selectedLocationId)
        assertNull(viewModel.errorMessage)
    }
}
