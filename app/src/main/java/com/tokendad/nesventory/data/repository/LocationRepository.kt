package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.LocationCreate
import com.tokendad.nesventory.data.remote.LocationPhoto
import com.tokendad.nesventory.data.remote.LocationUpdate
import com.tokendad.nesventory.data.remote.PaintInfo
import okhttp3.MultipartBody
import java.util.UUID

interface LocationRepository {
    suspend fun getLocations(page: Int? = null, limit: Int? = null): List<Location>
    suspend fun getLocation(id: UUID): Location
    suspend fun createLocation(location: LocationCreate): Location
    suspend fun updateLocation(id: UUID, location: LocationUpdate): Location
    suspend fun deleteLocation(id: UUID)
    suspend fun uploadLocationPhoto(locationId: UUID, file: MultipartBody.Part, isPrimary: Boolean = false, photoType: String? = null): LocationPhoto
    suspend fun deleteLocationPhoto(locationId: UUID, photoId: UUID)
    suspend fun getLocationCategories(): List<String>
    suspend fun parsePaintLabel(file: MultipartBody.Part): PaintInfo
}
