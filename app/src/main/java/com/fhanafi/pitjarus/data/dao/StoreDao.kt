package com.fhanafi.pitjarus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fhanafi.pitjarus.data.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores WHERE name LIKE '%' || :search || '%' OR code LIKE '%' || :search || '%' OR address LIKE '%' || :search || '%' ORDER BY name")
    fun observeStores(search: String): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    fun observeStore(id: Int): Flow<StoreEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stores: List<StoreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(store: StoreEntity)

    @Query("DELETE FROM stores WHERE id = :id")
    suspend fun deleteById(id: Int)
}
