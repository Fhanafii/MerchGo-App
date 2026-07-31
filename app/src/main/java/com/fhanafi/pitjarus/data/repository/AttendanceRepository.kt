package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.model.AttendanceReportRequest
import com.fhanafi.pitjarus.data.model.LocationPayload
import com.fhanafi.pitjarus.data.model.PhotoPayload
import com.fhanafi.pitjarus.datastore.UserPreference
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.isoTimestampNow
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    val checkedIn: Flow<Boolean> = userPreference.getAttendanceCheckedIn()

    suspend fun saveCheckedIn(checkedIn: Boolean) {
        userPreference.saveAttendanceCheckedIn(checkedIn)
    }

    suspend fun submitAttendance(
        type: AttendanceType,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        photoFileName: String,
        photoBase64: String
    ): NetworkResult<Unit> {
        val request = AttendanceReportRequest(
            clientReportId = UUID.randomUUID().toString(),
            attendanceType = type.apiValue,
            timestamp = isoTimestampNow(),
            location = LocationPayload(latitude, longitude, accuracy),
            photo = PhotoPayload(photoFileName, "image/jpeg", photoBase64)
        )
        return safeApiCall { apiService.submitAttendance(request) } as NetworkResult<Unit>
    }
}

enum class AttendanceType(val apiValue: String) {
    CHECK_IN("check_in"),
    CHECK_OUT("check_out")
}
