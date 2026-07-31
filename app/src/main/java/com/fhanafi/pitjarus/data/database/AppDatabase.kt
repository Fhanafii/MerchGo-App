package com.fhanafi.pitjarus.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fhanafi.pitjarus.data.dao.PlaceholderDao
import com.fhanafi.pitjarus.data.entity.PlaceholderEntity

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
}
