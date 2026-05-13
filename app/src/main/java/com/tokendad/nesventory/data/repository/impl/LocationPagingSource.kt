package com.tokendad.nesventory.data.repository.impl

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.repository.LocationRepository

class LocationPagingSource(
    private val repository: LocationRepository
) : PagingSource<Int, Location>() {
    private val networkPageSize = 30

    override fun getRefreshKey(state: PagingState<Int, Location>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Location> {
        return try {
            val page = params.key ?: 1
            val locations = repository.getLocations(page = page, limit = networkPageSize)
            LoadResult.Page(
                data = locations,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (locations.size < networkPageSize) null else page + 1
            )
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }
}
