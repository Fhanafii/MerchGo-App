package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.dao.ProductDao
import com.fhanafi.pitjarus.data.entity.ProductEntity
import com.fhanafi.pitjarus.data.entity.PendingActionType
import com.fhanafi.pitjarus.data.mapper.toEntity
import com.fhanafi.pitjarus.data.mapper.toUiModel
import com.fhanafi.pitjarus.data.model.AssignProductRequest
import com.fhanafi.pitjarus.data.model.CreateAndAssignProductRequest
import com.fhanafi.pitjarus.data.model.CreateProductRequest
import com.fhanafi.pitjarus.data.model.PendingAssignProductRequest
import com.fhanafi.pitjarus.data.model.ProductReportItem
import com.fhanafi.pitjarus.data.model.ProductReportRequest
import com.fhanafi.pitjarus.data.sync.PendingActionRepository
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
    private val productDao: ProductDao,
    private val pendingActionRepository: PendingActionRepository
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

    suspend fun createAndAssignProduct(
        storeId: Int,
        name: String,
        barcode: String,
        sku: String,
        size: String,
        price: Long
    ): NetworkResult<Unit> {
        val createRequest = CreateProductRequest(
            barcode = barcode,
            name = name,
            size = size,
            sku = sku,
            price = price
        )
        val createResult = safeApiCall { apiService.createProduct(createRequest) }
        return when {
            createResult is NetworkResult.Success -> {
                val product = createResult.data
                val assignResult = safeApiCall {
                    apiService.assignProductsToStore(storeId, AssignProductRequest(listOf(product.id)))
                }
                when {
                    assignResult is NetworkResult.Success -> {
                        productDao.upsert(product.toEntity(storeId))
                        NetworkResult.Success(Unit)
                    }
                    assignResult.isRetryableFailure() -> {
                        productDao.upsert(product.toEntity(storeId))
                        pendingActionRepository.enqueue(
                            PendingActionType.ASSIGN_PRODUCT,
                            PendingAssignProductRequest(storeId, product.id)
                        )
                        NetworkResult.Success(Unit)
                    }
                    else -> assignResult as NetworkResult<Unit>
                }
            }
            createResult.isRetryableFailure() -> {
                val offlineEntity = createRequest.toOfflineEntity(storeId)
                productDao.upsert(offlineEntity)
                pendingActionRepository.enqueue(
                    PendingActionType.CREATE_AND_ASSIGN_PRODUCT,
                    CreateAndAssignProductRequest(storeId, createRequest, offlineEntity.id)
                )
                NetworkResult.Success(Unit)
            }
            else -> createResult as NetworkResult<Unit>
        }
    }

    suspend fun submitReport(storeId: Int): NetworkResult<Unit> {
        val products = productDao.observeProducts(storeId).first()
        val request = ProductReportRequest(
            clientReportId = UUID.randomUUID().toString(),
            storeId = storeId,
            products = products.filter { it.id > 0 }.map(ProductEntity::toReportItem),
            timestamp = isoTimestampNow()
        )
        val result = safeApiCall { apiService.submitProductReport(request) }
        if (result.isRetryableFailure()) {
            pendingActionRepository.enqueue(PendingActionType.PRODUCT_REPORT, request)
            return NetworkResult.Success(Unit)
        }
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

private fun CreateProductRequest.toOfflineEntity(storeId: Int): ProductEntity {
    val temporaryId = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    return ProductEntity(
        id = temporaryId,
        storeId = storeId,
        barcode = barcode,
        name = name,
        sku = sku,
        size = size,
        price = price,
        available = true,
        normalPrice = null,
        promoPrice = null
    )
}
