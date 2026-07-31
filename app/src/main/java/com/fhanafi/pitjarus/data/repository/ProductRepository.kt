package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.dao.ProductDao
import com.fhanafi.pitjarus.data.entity.ProductEntity
import com.fhanafi.pitjarus.data.mapper.toEntity
import com.fhanafi.pitjarus.data.mapper.toUiModel
import com.fhanafi.pitjarus.data.model.ProductReportItem
import com.fhanafi.pitjarus.data.model.ProductReportRequest
import com.fhanafi.pitjarus.ui.model.ProductUiModel
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.isoTimestampNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao
) {
    fun observeProducts(storeId: Int): Flow<List<ProductUiModel>> {
        return productDao.observeProducts(storeId).map { products -> products.map { it.toUiModel() } }
    }

    suspend fun refreshProducts(storeId: Int): NetworkResult<Unit> {
        val result = safeApiCall { apiService.getStoreProducts(storeId) }
        if (result is NetworkResult.Success) {
            productDao.upsertAll(result.data.map { it.toEntity(storeId) })
            return NetworkResult.Success(Unit)
        }
        return result as NetworkResult<Unit>
    }

    suspend fun updateAvailability(storeId: Int, productId: Int, available: Boolean) {
        productDao.updateAvailability(storeId, productId, available)
    }

    suspend fun submitReport(storeId: Int): NetworkResult<Unit> {
        val products = productDao.observeProducts(storeId).first()
        val request = ProductReportRequest(
            clientReportId = UUID.randomUUID().toString(),
            storeId = storeId,
            products = products.map(ProductEntity::toReportItem),
            timestamp = isoTimestampNow()
        )
        val result = safeApiCall { apiService.submitProductReport(request) }
        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> result
            is NetworkResult.Unauthorized -> result
            is NetworkResult.ValidationError -> result
            NetworkResult.Loading -> NetworkResult.Loading
        }
    }
}

private fun ProductEntity.toReportItem() = ProductReportItem(id, available)
