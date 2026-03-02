package com.tokendad.nesventorynew.data.repository.impl

import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.LocationCreate
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.data.repository.LocationRepository
import java.util.UUID
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : LocationRepository {

    override suspend fun getLocations(): List<Location> = api.getLocations()

    override suspend fun getLocation(id: UUID): Location = api.getLocation(id)

    override suspend fun createLocation(location: LocationCreate): Location = api.createLocation(location)

    override suspend fun updateLocation(id: UUID, location: LocationCreate): Location =
        api.updateLocation(id, location)

    override suspend fun deleteLocation(id: UUID) = api.deleteLocation(id)

    override suspend fun getLocationCategories(): List<String> = api.getLocationCategories()
}
