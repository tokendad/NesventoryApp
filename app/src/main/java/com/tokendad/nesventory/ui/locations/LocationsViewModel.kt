package com.tokendad.nesventory.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.tokendad.nesventory.data.network.ConnectivityRepository
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.repository.LocationRepository
import com.tokendad.nesventory.data.repository.impl.LocationPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LocationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val preferencesManager: PreferencesManager,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _allLocations = MutableStateFlow<List<Location>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _currentParentId = MutableStateFlow<UUID?>(null)
    val currentParentId: StateFlow<UUID?> = _currentParentId.asStateFlow()
    private val navigationStack = mutableListOf<UUID?>()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    private val _pagingRefreshSignal = MutableStateFlow(0)

    val currentParent: StateFlow<Location?> = combine(_allLocations, _currentParentId) { all, parentId ->
        all.find { it.id == parentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val displayedLocations: StateFlow<List<Location>> = combine(
        _allLocations,
        _searchQuery,
        _currentParentId
    ) { all, query, parentId ->
        if (query.isNotBlank()) {
            all.filter {
                it.name.contains(query, ignoreCase = true) ||
                    (it.friendly_name?.contains(query, ignoreCase = true) == true)
            }
        } else {
            all.filter { it.parent_id == parentId }
                .sortedWith(
                    compareByDescending<Location> { it.is_primary_location }
                        .thenBy { it.name }
                )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pagedLocations = _pagingRefreshSignal.flatMapLatest {
        Pager(PagingConfig(pageSize = 30)) {
            LocationPagingSource(locationRepository)
        }.flow
    }.cachedIn(viewModelScope)

    init {
        fetchLocations()
        loadSettings()
        observeConnectivity()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val initial = preferencesManager.serverSettings.first()
            if (initial.remoteUrl.isNotBlank()) {
                _serverUrl.value = initial.remoteUrl.trimEnd('/')
            }
            preferencesManager.serverSettings.collect { settings ->
                if (settings.remoteUrl.isNotBlank()) {
                    _serverUrl.value = settings.remoteUrl.trimEnd('/')
                }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityRepository.isConnected.collect { connected ->
                _isOffline.value = !connected
            }
        }
    }

    fun fetchLocations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _allLocations.value = locationRepository.getLocations()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load locations: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun navigateTo(parentId: UUID?) {
        if (parentId != _currentParentId.value) {
            navigationStack.add(_currentParentId.value)
            _currentParentId.value = parentId
        }
    }

    fun navigateBack(): Boolean {
        if (navigationStack.isNotEmpty()) {
            _currentParentId.value = navigationStack.removeAt(navigationStack.size - 1)
            return true
        }
        return false
    }
    
    fun deleteLocation(locationId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                locationRepository.deleteLocation(locationId)
                fetchLocations()
                refreshPagedLocations()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete location: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun refreshPagedLocations() {
        _pagingRefreshSignal.value = _pagingRefreshSignal.value + 1
    }
}
