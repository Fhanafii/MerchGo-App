package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.entity.PendingActionType
import com.fhanafi.pitjarus.data.model.AttendanceReportRequest
import com.fhanafi.pitjarus.data.model.LocationPayload
import com.fhanafi.pitjarus.data.model.PhotoPayload
import com.fhanafi.pitjarus.data.sync.PendingActionRepository
import com.fhanafi.pitjarus.datastore.AttendanceSession
import com.fhanafi.pitjarus.datastore.UserPreference
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.isoTimestampNow
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
    private val pendingActionRepository: PendingActionRepository
) {
    val checkedIn: Flow<Boolean> = userPreference.getAttendanceCheckedIn()
    val attendanceSession: Flow<AttendanceSession> = userPreference.getAttendanceSession()

    suspend fun saveCheckedIn(checkedIn: Boolean) {
        userPreference.saveAttendanceCheckedIn(checkedIn)
    }

    suspend fun saveAttendanceSession(session: AttendanceSession) {
        userPreference.saveAttendanceSession(session)
    }

    suspend fun submitAttendance(
        type: AttendanceType,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        photoFileName: String,
        photoBase64: String
    ): NetworkResult<AttendanceSubmission> {
        val clientReportId = UUID.randomUUID().toString()
        val timestamp = isoTimestampNow()
        val request = AttendanceReportRequest(
            clientReportId = clientReportId,
            attendanceType = type.apiValue,
            timestamp = timestamp,
            location = LocationPayload(latitude, longitude, accuracy),
            photo = PhotoPayload(photoFileName, "image/jpeg", photoBase64)
        )
        val result = safeApiCall { apiService.submitAttendance(request) } as NetworkResult<Unit>
        return if (result.isRetryableFailure()) {
            pendingActionRepository.enqueue(PendingActionType.ATTENDANCE_REPORT, request)
            NetworkResult.Success(AttendanceSubmission(clientReportId, timestamp))
        } else {
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(AttendanceSubmission(clientReportId, timestamp))
                is NetworkResult.Error -> result
                is NetworkResult.Unauthorized -> result
                is NetworkResult.ValidationError -> result
                NetworkResult.Loading -> NetworkResult.Loading
            }
        }
    }
}

data class AttendanceSubmission(
    val attendanceId: String,
    val timestamp: String
)

enum class AttendanceType(val apiValue: String) {
    CHECK_IN("check_in"),
    CHECK_OUT("check_out")
}
