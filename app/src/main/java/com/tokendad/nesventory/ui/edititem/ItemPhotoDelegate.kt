package com.tokendad.nesventory.ui.edititem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tokendad.nesventory.data.remote.Photo
import com.tokendad.nesventory.data.repository.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class ItemPhotoDelegate(
    private val itemRepository: ItemRepository,
    private val scope: CoroutineScope,
    private val onError: (String) -> Unit,
    private val onLoadingChange: (Boolean) -> Unit
) {
    var itemMedia by mutableStateOf<List<Photo>>(emptyList())
        private set

    fun replaceItemMedia(photos: List<Photo>) {
        itemMedia = photos
    }

    fun deletePhoto(itemId: UUID, photoId: UUID, onSuccess: () -> Unit) {
        scope.launch {
            onLoadingChange(true)
            try {
                itemRepository.deleteItemPhoto(itemId, photoId)
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to delete photo: ${e.localizedMessage}")
            } finally {
                onLoadingChange(false)
            }
        }
    }
}
