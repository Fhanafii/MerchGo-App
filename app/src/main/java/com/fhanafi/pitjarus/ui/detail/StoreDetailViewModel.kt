package com.fhanafi.pitjarus.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.StoreRepository
import com.fhanafi.pitjarus.ui.model.StoreUiModel
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<StoreUiModel>>(UiState.Loading)
    val uiState: StateFlow<UiState<StoreUiModel>> = _uiState.asStateFlow()

    fun loadStore(storeId: Int) {
        viewModelScope.launch {
            storeRepository.observeStore(storeId).collect { store ->
                _uiState.value = store?.let { UiState.Success(it) } ?: UiState.Empty
            }
        }
    }
}
