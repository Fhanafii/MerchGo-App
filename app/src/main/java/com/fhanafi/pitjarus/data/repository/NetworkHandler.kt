package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.model.ApiResponse
import com.fhanafi.pitjarus.utils.NetworkResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(call: suspend () -> Response<ApiResponse<T>>): NetworkResult<T> {
    return try {
        val response = call()
        val body = response.body()
        when {
            response.isSuccessful && body?.success == true && body.data != null -> NetworkResult.Success(body.data)
            response.isSuccessful && body?.success == true -> NetworkResult.Success(Unit as T)
            response.code() == 401 -> NetworkResult.Unauthorized()
            response.code() == 400 -> {
                val error = parseError(response)
                NetworkResult.ValidationError(error.message, error.errors)
            }
            else -> {
                val error = parseError(response)
                NetworkResult.Error(error.message, code = response.code(), errors = error.errors)
            }
        }
    } catch (exception: SocketTimeoutException) {
        NetworkResult.Error("Koneksi timeout", exception)
    } catch (exception: IOException) {
        NetworkResult.Error("Tidak ada koneksi internet", exception)
    } catch (exception: Exception) {
        NetworkResult.Error(exception.message ?: "Terjadi kesalahan tidak terduga", exception)
    }
}

fun NetworkResult<*>.isRetryableFailure(): Boolean {
    return this is NetworkResult.Error && (code == null || code >= 500)
}

private data class ParsedError(val message: String, val errors: List<String>)

private fun <T> parseError(response: Response<ApiResponse<T>>): ParsedError {
    val raw = response.errorBody()?.string().orEmpty()
    if (raw.isBlank()) return ParsedError(response.message().ifBlank { "Request gagal" }, emptyList())
    return runCatching {
        val type = object : TypeToken<ApiResponse<Unit>>() {}.type
        val error = Gson().fromJson<ApiResponse<Unit>>(raw, type)
        ParsedError(error.message, error.errors.orEmpty())
    }.getOrElse {
        ParsedError(response.message().ifBlank { "Request gagal" }, emptyList())
    }
}
