package com.fhanafi.pitjarus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fhanafi.pitjarus.data.entity.PromoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromoDao {
    @Query("SELECT * FROM promos WHERE storeId = :storeId ORDER BY id DESC")
    fun observePromos(storeId: Int): Flow<List<PromoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promo: PromoEntity)

    @Query("DELETE FROM promos WHERE storeId = :storeId")
    suspend fun clearByStore(storeId: Int)
}
