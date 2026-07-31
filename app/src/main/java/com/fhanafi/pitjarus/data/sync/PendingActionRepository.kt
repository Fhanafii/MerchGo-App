package com.fhanafi.pitjarus.data.sync

import com.fhanafi.pitjarus.data.dao.PendingActionDao
import com.fhanafi.pitjarus.data.entity.PendingActionEntity
import com.fhanafi.pitjarus.data.entity.PendingActionStatus
import com.fhanafi.pitjarus.data.entity.PendingActionType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingActionRepository @Inject constructor(
    private val pendingActionDao: PendingActionDao,
    private val syncScheduler: SyncScheduler,
    private val gson: Gson
) {
    fun observePendingCount(): Flow<Int> {
        return pendingActionDao.observePendingCount(listOf(PendingActionStatus.PENDING, PendingActionStatus.FAILED))
    }

    suspend fun enqueue(type: PendingActionType, request: Any) {
        val id = pendingActionDao.insert(
            PendingActionEntity(
                type = type.name,
                body = gson.toJson(request),
                createdAt = System.currentTimeMillis()
            )
        )
        Timber.d("Queue insert: id=%s type=%s", id, type)
        syncScheduler.enqueue()
    }
}
