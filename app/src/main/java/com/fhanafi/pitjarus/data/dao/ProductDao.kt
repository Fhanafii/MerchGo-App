package com.fhanafi.pitjarus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fhanafi.pitjarus.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY name")
    fun observeProducts(storeId: Int): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("UPDATE products SET available = :available WHERE id = :productId AND storeId = :storeId")
    suspend fun updateAvailability(storeId: Int, productId: Int, available: Boolean)
}
