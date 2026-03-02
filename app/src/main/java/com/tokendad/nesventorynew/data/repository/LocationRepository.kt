package com.tokendad.nesventorynew.data.repository

import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.LocationCreate
import java.util.UUID

interface LocationRepository {
    suspend fun getLocations(): List<Location>
    suspend fun getLocation(id: UUID): Location
    suspend fun createLocation(location: LocationCreate): Location
    suspend fun updateLocation(id: UUID, location: LocationCreate): Location
    suspend fun deleteLocation(id: UUID)
    suspend fun getLocationCategories(): List<String>
}
