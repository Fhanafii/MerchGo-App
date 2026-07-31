package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.dao.StoreDao
import com.fhanafi.pitjarus.data.mapper.toEntity
import com.fhanafi.pitjarus.data.mapper.toUiModel
import com.fhanafi.pitjarus.ui.model.StoreUiModel
import com.fhanafi.pitjarus.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StoreRepository @Inject constructor(
    private val apiService: ApiService,
    private val storeDao: StoreDao
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
}
