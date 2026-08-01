package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.model.LoginDto
import com.fhanafi.pitjarus.data.model.LoginRequest
import com.fhanafi.pitjarus.datastore.UserPreference
import com.fhanafi.pitjarus.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    val isLoggedIn: Flow<Boolean> = userPreference.getLoginState()

    suspend fun login(username: String, password: String): NetworkResult<LoginDto> {
        val result = safeApiCall { apiService.login(LoginRequest(username, password)) }
        if (result is NetworkResult.Success) {
            userPreference.saveSession(result.data.token, result.data.id, result.data.name, result.data.expiredAt)
        }
        return result
    }

    suspend fun logout() {
        userPreference.clearSession()
    }
}
