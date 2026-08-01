package com.fhanafi.pitjarus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.AttendanceRepository
import com.fhanafi.pitjarus.data.repository.AttendanceType
import com.fhanafi.pitjarus.datastore.AttendanceSession
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    private val _events = MutableSharedFlow<AttendanceEvent>()
    val events: SharedFlow<AttendanceEvent> = _events.asSharedFlow()

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            attendanceRepository.attendanceSession.collect { session ->
                _uiState.value = _uiState.value.copy(session = session)
            }
        }
    }

    fun submit(
        type: AttendanceType,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        photoFileName: String,
        photoBase64: String
    ) {
        if (_uiState.value.submitState is UiState.Loading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                submitState = UiState.Loading,
                loadingMessage = if (type == AttendanceType.CHECK_IN) "Checking in..." else "Checking out..."
            )
            when (val result = attendanceRepository.submitAttendance(
                type,
                latitude,
                longitude,
                accuracy,
                photoFileName,
                photoBase64
            )) {
                is NetworkResult.Success -> {
                    attendanceRepository.saveAttendanceSession(
                        if (type == AttendanceType.CHECK_IN) {
                            AttendanceSession(
                                checkedIn = true,
                                checkedOut = false,
                                attendanceId = result.data.attendanceId,
                                checkInTime = result.data.timestamp
                            )
                        } else {
                            AttendanceSession(
                                checkedIn = false,
                                checkedOut = true,
                                attendanceId = null,
                                checkInTime = null
                            )
                        }
                    )
                    _events.emit(AttendanceEvent.Success(type))
                }
                is NetworkResult.Error -> _events.emit(AttendanceEvent.Error(result.message))
                is NetworkResult.ValidationError -> _events.emit(AttendanceEvent.Error(result.message))
                is NetworkResult.Unauthorized -> _events.emit(AttendanceEvent.Unauthorized(result.message))
                NetworkResult.Loading -> Unit
            }
            _uiState.value = _uiState.value.copy(submitState = UiState.Idle, loadingMessage = null)
        }
    }
}

data class AttendanceUiState(
    val session: AttendanceSession = AttendanceSession(),
    val submitState: UiState<Unit> = UiState.Idle,
    val loadingMessage: String? = null
)

sealed class AttendanceEvent {
    data class Success(val type: AttendanceType) : AttendanceEvent()
    data class Error(val message: String) : AttendanceEvent()
    data class Unauthorized(val message: String) : AttendanceEvent()
}
