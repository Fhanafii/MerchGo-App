package com.fhanafi.pitjarus.utils

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val code: Int? = null,
        val errors: List<String> = emptyList()
    ) : NetworkResult<Nothing>()

    data class Unauthorized(val message: String = "Sesi berakhir, silakan login kembali") : NetworkResult<Nothing>()
    data class ValidationError(val message: String, val errors: List<String> = emptyList()) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}
