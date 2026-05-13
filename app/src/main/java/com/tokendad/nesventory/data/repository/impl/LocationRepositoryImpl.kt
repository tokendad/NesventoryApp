package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.LocationCreate
import com.tokendad.nesventory.data.remote.LocationPhoto
import com.tokendad.nesventory.data.remote.LocationUpdate
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.repository.LocationRepository
import okhttp3.MultipartBody
import java.util.UUID
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : LocationRepository {

    override suspend fun getLocations(): List<Location> = api.getLocations()

    override suspend fun getLocation(id: UUID): Location = api.getLocation(id)

    override suspend fun createLocation(location: LocationCreate): Location = api.createLocation(location)

    override suspend fun updateLocation(id: UUID, location: LocationUpdate): Location =
        api.updateLocation(id, location)

    override suspend fun deleteLocation(id: UUID) = api.deleteLocation(id)

    override suspend fun uploadLocationPhoto(
        locationId: UUID,
        file: MultipartBody.Part,
        isPrimary: Boolean,
        photoType: String?
    ): LocationPhoto = api.uploadLocationPhoto(locationId, file, isPrimary, photoType)

    override suspend fun deleteLocationPhoto(locationId: UUID, photoId: UUID) {
        api.deleteLocationPhoto(locationId, photoId)
    }

    override suspend fun getLocationCategories(): List<String> = api.getLocationCategories()
}
