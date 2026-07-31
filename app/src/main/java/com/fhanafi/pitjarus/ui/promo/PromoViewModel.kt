package com.fhanafi.pitjarus.ui.promo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.PromoRepository
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
    private val promoRepository: PromoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<PromoUiModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<PromoUiModel>>> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun observePromos(storeId: Int) {
        viewModelScope.launch {
            promoRepository.observePromos(storeId).collect { promos ->
                _uiState.value = if (promos.isEmpty()) UiState.Empty else UiState.Success(promos)
            }
        }
    }

    fun addPromo(storeId: Int, productName: String, normalPriceText: String, promoPriceText: String): String? {
        val normalPrice = normalPriceText.toLongOrNull()
        val promoPrice = promoPriceText.toLongOrNull()
        return when {
            productName.isBlank() -> "Nama produk wajib diisi"
            normalPrice == null || normalPrice <= 0 -> "Harga normal tidak valid"
            promoPrice == null || promoPrice <= 0 -> "Harga promo tidak valid"
            promoPrice >= normalPrice -> "Harga promo harus lebih kecil dari harga normal"
            else -> {
                viewModelScope.launch {
                    promoRepository.addPromo(storeId, productName.trim(), normalPrice, promoPrice)
                }
                null
            }
        }
    }

    fun submitReport(storeId: Int) {
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
