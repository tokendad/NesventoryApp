package com.tokendad.nesventory.data.repository.impl

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.repository.ItemRepository
import java.util.UUID

class ItemPagingSource(
    private val repository: ItemRepository,
    private val search: String?,
    private val locationId: UUID?,
    private val isLiving: Boolean?,
    private val relationshipType: String?,
    private val collectionId: UUID?,
    private val collectionIdRecursive: Boolean?
) : PagingSource<Int, Item>() {
    private val networkPageSize = 30

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        return try {
            val page = params.key ?: 1
            val items = repository.getItems(
                search = search,
                locationId = locationId,
                isLiving = isLiving,
                relationshipType = relationshipType,
                collectionId = collectionId,
                collectionIdRecursive = collectionIdRecursive,
                page = page,
                limit = networkPageSize
            )
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.size < networkPageSize) null else page + 1
            )
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }
}
