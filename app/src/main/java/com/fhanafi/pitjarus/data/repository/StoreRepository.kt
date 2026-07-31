package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.dao.StoreDao
import com.fhanafi.pitjarus.data.entity.PendingActionType
import com.fhanafi.pitjarus.data.entity.StoreEntity
import com.fhanafi.pitjarus.data.mapper.toEntity
import com.fhanafi.pitjarus.data.mapper.toUiModel
import com.fhanafi.pitjarus.data.model.CreateStoreRequest
import com.fhanafi.pitjarus.data.model.PendingCreateStoreRequest
import com.fhanafi.pitjarus.data.sync.PendingActionRepository
import com.fhanafi.pitjarus.ui.model.StoreUiModel
import com.fhanafi.pitjarus.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StoreRepository @Inject constructor(
    private val apiService: ApiService,
    private val storeDao: StoreDao,
    private val pendingActionRepository: PendingActionRepository
) {
    fun observeStores(search: String): Flow<List<StoreUiModel>> {
        return storeDao.observeStores(search).map { stores -> stores.map { it.toUiModel() } }
    }

    fun observeStore(id: Int): Flow<StoreUiModel?> {
        return storeDao.observeStore(id).map { it?.toUiModel() }
    }

    suspend fun refreshStores(search: String = ""): NetworkResult<Unit> {
        val result = safeApiCall { apiService.getStores(search = search) }
        if (result is NetworkResult.Success) {
            storeDao.upsertAll(result.data.map { it.toEntity() })
            return NetworkResult.Success(Unit)
        }
        return result as NetworkResult<Unit>
    }

    suspend fun createStore(
        code: String,
        name: String,
        address: String,
        latitude: Double,
        longitude: Double
    ): NetworkResult<Unit> {
        val request = CreateStoreRequest(code, name, address, latitude, longitude)
        val result = safeApiCall { apiService.createStore(request) }
        return when {
            result is NetworkResult.Success -> {
                storeDao.upsert(result.data.toEntity())
                NetworkResult.Success(Unit)
            }
            result.isRetryableFailure() -> {
                val offlineEntity = request.toOfflineEntity()
                storeDao.upsert(offlineEntity)
                pendingActionRepository.enqueue(
                    PendingActionType.CREATE_STORE,
                    PendingCreateStoreRequest(offlineEntity.id, request)
                )
                NetworkResult.Success(Unit)
            }
            else -> result as NetworkResult<Unit>
        }
    }
}

private fun CreateStoreRequest.toOfflineEntity(): StoreEntity {
    val temporaryId = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    return StoreEntity(
        id = temporaryId,
        code = code,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        isActive = true
    )
}
