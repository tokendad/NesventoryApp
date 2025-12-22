package com.example.nesventorynew.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.remote.Item
import com.example.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    var items by mutableStateOf<List<Item>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                items = api.getItems()
            } catch (e: Exception) {
                errorMessage = "Failed to load items: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}