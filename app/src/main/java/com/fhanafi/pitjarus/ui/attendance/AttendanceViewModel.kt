package com.fhanafi.pitjarus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fhanafi.pitjarus.data.repository.AttendanceRepository
import com.fhanafi.pitjarus.data.repository.AttendanceType
import com.fhanafi.pitjarus.utils.NetworkResult
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

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _checkedIn = MutableStateFlow(false)
    val checkedIn: StateFlow<Boolean> = _checkedIn.asStateFlow()

    init {
        viewModelScope.launch {
            attendanceRepository.checkedIn.collect { _checkedIn.value = it }
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
        viewModelScope.launch {
            _loading.value = true
            when (val result = attendanceRepository.submitAttendance(
                type,
                latitude,
                longitude,
                accuracy,
                photoFileName,
                photoBase64
            )) {
                is NetworkResult.Success -> {
                    val checkedInAfterSubmit = type == AttendanceType.CHECK_IN
                    attendanceRepository.saveCheckedIn(checkedInAfterSubmit)
                    _events.emit(AttendanceEvent.Success(type))
                }
                is NetworkResult.Error -> _events.emit(AttendanceEvent.Error(result.message))
                is NetworkResult.ValidationError -> _events.emit(AttendanceEvent.Error(result.message))
                is NetworkResult.Unauthorized -> _events.emit(AttendanceEvent.Unauthorized(result.message))
                NetworkResult.Loading -> Unit
            }
            _loading.value = false
        }
    }
}

sealed class AttendanceEvent {
    data class Success(val type: AttendanceType) : AttendanceEvent()
    data class Error(val message: String) : AttendanceEvent()
    data class Unauthorized(val message: String) : AttendanceEvent()
}
