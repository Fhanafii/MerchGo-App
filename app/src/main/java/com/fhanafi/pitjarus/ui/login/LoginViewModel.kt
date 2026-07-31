package com.fhanafi.pitjarus.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.AuthRepository
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    fun checkSession() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn.first()) {
                _uiState.value = UiState.Success(Unit)
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Username dan password wajib diisi")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = authRepository.login(username, password)) {
                is NetworkResult.Success -> UiState.Success(Unit)
                is NetworkResult.ValidationError -> UiState.Error(result.message)
                is NetworkResult.Unauthorized -> UiState.Unauthorized(result.message)
                is NetworkResult.Error -> UiState.Error(result.message)
                NetworkResult.Loading -> UiState.Loading
            }
        }
    }
}
