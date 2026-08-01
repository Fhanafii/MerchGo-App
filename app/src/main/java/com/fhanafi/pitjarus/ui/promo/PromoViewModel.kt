package com.fhanafi.pitjarus.ui.promo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.ProductRepository
import com.fhanafi.pitjarus.data.repository.PromoRepository
import com.fhanafi.pitjarus.ui.model.ProductUiModel
import com.fhanafi.pitjarus.ui.model.PromoUiModel
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromoViewModel @Inject constructor(
    private val promoRepository: PromoRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<PromoUiModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<PromoUiModel>>> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    private val _addState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val addState: StateFlow<UiState<Unit>> = _addState.asStateFlow()

    private val _products = MutableStateFlow<List<ProductUiModel>>(emptyList())
    val products: StateFlow<List<ProductUiModel>> = _products.asStateFlow()

    fun observePromos(storeId: Int) {
        viewModelScope.launch {
            promoRepository.observePromos(storeId).collect { promos ->
                _uiState.value = if (promos.isEmpty()) UiState.Empty else UiState.Success(promos)
            }
        }
        viewModelScope.launch {
            productRepository.observeProducts(storeId).collect { products ->
                _products.value = products
            }
        }
        productRepositoryRefresh(storeId)
    }

    private fun productRepositoryRefresh(storeId: Int) {
        viewModelScope.launch {
            productRepository.refreshProducts(storeId)
        }
    }

    fun addPromo(storeId: Int, productDisplay: String, normalPriceText: String, promoPriceText: String): String? {
        if (_addState.value is UiState.Loading) return null
        val productName = productDisplay.substringBefore(" - ").trim()
        val normalPrice = normalPriceText.toLongOrNull()
        val promoPrice = promoPriceText.toLongOrNull()
        return when {
            productName.isBlank() -> "Nama produk wajib diisi"
            _products.value.none { it.name == productName } -> "Pilih produk dari daftar"
            normalPrice == null || normalPrice <= 0 -> "Harga normal tidak valid"
            promoPrice == null || promoPrice <= 0 -> "Harga promo tidak valid"
            promoPrice >= normalPrice -> "Harga promo harus lebih kecil dari harga normal"
            else -> {
                viewModelScope.launch {
                    _addState.value = UiState.Loading
                    promoRepository.addPromo(storeId, productName.trim(), normalPrice, promoPrice)
                    _addState.value = UiState.Success(Unit)
                }
                null
            }
        }
    }

    fun clearAddState() {
        _addState.value = UiState.Idle
    }

    fun submitReport(storeId: Int) {
        if (_submitState.value is UiState.Loading) return
        viewModelScope.launch {
            _submitState.value = UiState.Loading
            _submitState.value = when (val result = promoRepository.submitReport(storeId)) {
                is NetworkResult.Success -> UiState.Success(Unit)
                is NetworkResult.Error -> UiState.Error(result.message)
                is NetworkResult.ValidationError -> UiState.Error(result.message)
                is NetworkResult.Unauthorized -> UiState.Unauthorized(result.message)
                NetworkResult.Loading -> UiState.Loading
            }
        }
    }
}
