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

    private val _createState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createState: StateFlow<UiState<Unit>> = _createState.asStateFlow()

    private val _availabilityLoadingIds = MutableStateFlow<Set<Int>>(emptySet())
    val availabilityLoadingIds: StateFlow<Set<Int>> = _availabilityLoadingIds.asStateFlow()

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
            if (_uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            }
            when (val result = productRepository.refreshProducts(storeId)) {
                is NetworkResult.Error -> if (_uiState.value !is UiState.Success) _uiState.value = UiState.Error(result.message)
                is NetworkResult.Unauthorized -> _uiState.value = UiState.Unauthorized(result.message)
                is NetworkResult.ValidationError -> _uiState.value = UiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun updateAvailability(storeId: Int, productId: Int, available: Boolean) {
        if (productId in _availabilityLoadingIds.value) return
        viewModelScope.launch {
            _availabilityLoadingIds.value = _availabilityLoadingIds.value + productId
            productRepository.updateAvailability(storeId, productId, available)
            _availabilityLoadingIds.value = _availabilityLoadingIds.value - productId
        }
    }

    fun createAndAssignProduct(
        storeId: Int,
        name: String,
        barcode: String,
        sku: String,
        size: String,
        priceText: String
    ): String? {
        if (_createState.value is UiState.Loading) return null
        val price = priceText.toLongOrNull()
        return when {
            name.isBlank() -> "Nama produk wajib diisi"
            barcode.isBlank() -> "Barcode wajib diisi"
            sku.isBlank() -> "SKU wajib diisi"
            size.isBlank() -> "Ukuran wajib diisi"
            price == null || price < 0 -> "Harga tidak valid"
            else -> {
                viewModelScope.launch {
                    _createState.value = UiState.Loading
                    _createState.value = when (
                        val result = productRepository.createAndAssignProduct(
                            storeId = storeId,
                            name = name.trim(),
                            barcode = barcode.trim(),
                            sku = sku.trim(),
                            size = size.trim(),
                            price = price
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

    fun submitReport(storeId: Int) {
        if (_submitState.value is UiState.Loading) return
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
