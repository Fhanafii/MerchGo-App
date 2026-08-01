package com.fhanafi.pitjarus.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fhanafi.pitjarus.data.dao.PendingActionDao
import com.fhanafi.pitjarus.data.dao.ProductDao
import com.fhanafi.pitjarus.data.dao.PromoDao
import com.fhanafi.pitjarus.data.dao.StoreDao
import com.fhanafi.pitjarus.data.entity.PendingActionEntity
import com.fhanafi.pitjarus.data.entity.ProductEntity
import com.fhanafi.pitjarus.data.entity.PromoEntity
import com.fhanafi.pitjarus.data.entity.StoreEntity

@Database(
    entities = [StoreEntity::class, ProductEntity::class, PromoEntity::class, PendingActionEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun promoDao(): PromoDao
    abstract fun pendingActionDao(): PendingActionDao
}
