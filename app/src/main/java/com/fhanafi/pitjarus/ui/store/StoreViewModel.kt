package com.fhanafi.pitjarus.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.StoreRepository
import com.fhanafi.pitjarus.ui.model.StoreUiModel
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow<UiState<List<StoreUiModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<StoreUiModel>>> = _uiState.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createState: StateFlow<UiState<Unit>> = _createState.asStateFlow()

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(250)
                .flatMapLatest { storeRepository.observeStores(it) }
                .collect { stores ->
                    _uiState.value = if (stores.isEmpty()) UiState.Empty else UiState.Success(stores)
                }
        }
        refresh()
    }

    fun search(query: String) {
        searchQuery.value = query
        refresh(query)
    }

    fun refresh(search: String = searchQuery.value) {
        viewModelScope.launch {
            if (_uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            }
            when (val result = storeRepository.refreshStores(search)) {
                is NetworkResult.Error -> if (_uiState.value !is UiState.Success) _uiState.value = UiState.Error(result.message)
                is NetworkResult.Unauthorized -> _uiState.value = UiState.Unauthorized(result.message)
                is NetworkResult.ValidationError -> _uiState.value = UiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun createStore(
        code: String,
        name: String,
        address: String,
        latitude: Double,
        longitude: Double
    ): String? {
        if (_createState.value is UiState.Loading) return null
        return when {
            code.isBlank() -> "Kode toko wajib diisi"
            name.isBlank() -> "Nama toko wajib diisi"
            address.isBlank() -> "Alamat toko wajib diisi"
            else -> {
                viewModelScope.launch {
                    _createState.value = UiState.Loading
                    _createState.value = when (
                        val result = storeRepository.createStore(
                            code.trim(),
                            name.trim(),
                            address.trim(),
                            latitude,
                            longitude
                        )
                    ) {
                        is NetworkResult.Success -> UiState.Success(Unit)
                        is NetworkResult.Error -> UiState.Error(result.message)
                        is NetworkResult.ValidationError -> UiState.Error(result.message)
                        is NetworkResult.Unauthorized -> UiState.Unauthorized(result.message)
                        NetworkResult.Loading -> UiState.Loading
                    }
                }
                null
            }
        }
    }

    fun clearCreateState() {
        _createState.value = UiState.Idle
    }
}
