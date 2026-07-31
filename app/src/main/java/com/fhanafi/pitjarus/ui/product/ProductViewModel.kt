package com.fhanafi.pitjarus.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.ProductRepository
import com.fhanafi.pitjarus.ui.model.ProductUiModel
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<ProductUiModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ProductUiModel>>> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun loadProducts(storeId: Int) {
        viewModelScope.launch {
            productRepository.observeProducts(storeId).collect { products ->
                _uiState.value = if (products.isEmpty()) UiState.Empty else UiState.Success(products)
            }
        }
        refresh(storeId)
    }

    fun refresh(storeId: Int) {
        viewModelScope.launch {
            when (val result = productRepository.refreshProducts(storeId)) {
                is NetworkResult.Error -> if (_uiState.value !is UiState.Success) _uiState.value = UiState.Error(result.message)
                is NetworkResult.Unauthorized -> _uiState.value = UiState.Unauthorized(result.message)
                is NetworkResult.ValidationError -> _uiState.value = UiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun updateAvailability(storeId: Int, productId: Int, available: Boolean) {
        viewModelScope.launch {
            productRepository.updateAvailability(storeId, productId, available)
        }
    }

    fun submitReport(storeId: Int) {
        viewModelScope.launch {
            _submitState.value = UiState.Loading
            _submitState.value = when (val result = productRepository.submitReport(storeId)) {
                is NetworkResult.Success -> UiState.Success(Unit)
                is NetworkResult.Error -> UiState.Error(result.message)
                is NetworkResult.ValidationError -> UiState.Error(result.message)
                is NetworkResult.Unauthorized -> UiState.Unauthorized(result.message)
                NetworkResult.Loading -> UiState.Loading
            }
        }
    }
}
