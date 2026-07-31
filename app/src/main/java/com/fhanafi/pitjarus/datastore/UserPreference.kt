package com.fhanafi.pitjarus.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fhanafi.pitjarus.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = Constants.USER_PREFERENCES_NAME)

class UserPreference @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    fun getLoginState(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }
    }

    fun getUserName(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }
    }

    fun getAttendanceCheckedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[ATTENDANCE_CHECKED_IN_KEY] ?: false
        }
    }

    suspend fun saveSession(token: String, userId: Int, name: String, expiredAt: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = name
            preferences[EXPIRED_AT_KEY] = expiredAt
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    suspend fun saveLoginState(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }

    suspend fun saveAttendanceCheckedIn(checkedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ATTENDANCE_CHECKED_IN_KEY] = checkedIn
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
        val USER_ID_KEY = intPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val EXPIRED_AT_KEY = stringPreferencesKey("expired_at")
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        val ATTENDANCE_CHECKED_IN_KEY = booleanPreferencesKey("attendance_checked_in")
    }
}
