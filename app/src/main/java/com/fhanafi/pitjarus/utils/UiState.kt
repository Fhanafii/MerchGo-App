package com.fhanafi.pitjarus.utils

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    data class Unauthorized(val message: String = "Sesi berakhir, silakan login kembali") : UiState<Nothing>()
}
