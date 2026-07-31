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
            when (val result = storeRepository.refreshStores(search)) {
                is NetworkResult.Error -> if (_uiState.value !is UiState.Success) _uiState.value = UiState.Error(result.message)
                is NetworkResult.Unauthorized -> _uiState.value = UiState.Unauthorized(result.message)
                is NetworkResult.ValidationError -> _uiState.value = UiState.Error(result.message)
                else -> Unit
            }
        }
    }
}
