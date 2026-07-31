package com.fhanafi.pitjarus.data.database

import androidx.room.TypeConverter
import com.fhanafi.pitjarus.data.entity.PendingActionStatus

class RoomConverters {
    @TypeConverter
    fun toPendingActionStatus(value: String): PendingActionStatus {
        return PendingActionStatus.valueOf(value)
    }

    @TypeConverter
    fun fromPendingActionStatus(value: PendingActionStatus): String {
        return value.name
    }
}
