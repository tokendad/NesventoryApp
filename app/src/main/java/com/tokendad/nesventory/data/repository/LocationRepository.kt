package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.LocationCreate
import com.tokendad.nesventory.data.remote.LocationUpdate
import java.util.UUID

interface LocationRepository {
    suspend fun getLocations(): List<Location>
    suspend fun getLocation(id: UUID): Location
    suspend fun createLocation(location: LocationCreate): Location
    suspend fun updateLocation(id: UUID, location: LocationUpdate): Location
    suspend fun deleteLocation(id: UUID)
    suspend fun getLocationCategories(): List<String>
}
