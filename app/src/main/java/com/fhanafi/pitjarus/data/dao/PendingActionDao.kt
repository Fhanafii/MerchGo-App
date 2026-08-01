package com.fhanafi.pitjarus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fhanafi.pitjarus.data.entity.PendingActionEntity
import com.fhanafi.pitjarus.data.entity.PendingActionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingActionDao {
    @Query("SELECT * FROM pending_actions WHERE status IN (:statuses) ORDER BY createdAt ASC")
    suspend fun getByStatuses(statuses: List<PendingActionStatus>): List<PendingActionEntity>

    @Query("SELECT COUNT(*) FROM pending_actions WHERE status IN (:statuses)")
    fun observePendingCount(statuses: List<PendingActionStatus>): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: PendingActionEntity): Long

    @Update
    suspend fun update(action: PendingActionEntity)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
