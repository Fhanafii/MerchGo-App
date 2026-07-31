package com.fhanafi.pitjarus.data.sync

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.dao.PendingActionDao
import com.fhanafi.pitjarus.data.dao.ProductDao
import com.fhanafi.pitjarus.data.dao.StoreDao
import com.fhanafi.pitjarus.data.entity.PendingActionEntity
import com.fhanafi.pitjarus.data.entity.PendingActionStatus
import com.fhanafi.pitjarus.data.entity.PendingActionType
import com.fhanafi.pitjarus.data.mapper.toEntity
import com.fhanafi.pitjarus.data.model.AssignProductRequest
import com.fhanafi.pitjarus.data.model.AttendanceReportRequest
import com.fhanafi.pitjarus.data.model.CreateAndAssignProductRequest
import com.fhanafi.pitjarus.data.model.PendingAssignProductRequest
import com.fhanafi.pitjarus.data.model.PendingCreateStoreRequest
import com.fhanafi.pitjarus.data.model.ProductReportRequest
import com.fhanafi.pitjarus.data.model.PromoReportRequest
import com.fhanafi.pitjarus.data.repository.safeApiCall
import com.fhanafi.pitjarus.utils.NetworkResult
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

class PendingActionSyncer @Inject constructor(
    private val pendingActionDao: PendingActionDao,
    private val storeDao: StoreDao,
    private val productDao: ProductDao,
    private val apiService: ApiService,
    private val gson: Gson
) {
    suspend fun sync(): SyncOutcome {
        val actions = pendingActionDao.getByStatuses(listOf(PendingActionStatus.PENDING, PendingActionStatus.FAILED))
        if (actions.isEmpty()) return SyncOutcome.Success

        var shouldRetryWorker = false
        actions.forEach { action ->
            val outcome = syncAction(action)
            if (outcome == ActionOutcome.RetryLater) {
                shouldRetryWorker = true
            }
        }
        return if (shouldRetryWorker) SyncOutcome.Retry else SyncOutcome.Success
    }

    private suspend fun syncAction(action: PendingActionEntity): ActionOutcome {
        pendingActionDao.update(action.copy(status = PendingActionStatus.SYNCING, lastError = null))
        Timber.d("Synchronization start: id=%s type=%s retry=%s", action.id, action.type, action.retryCount)

        val result = runCatching { upload(action) }.getOrElse { exception ->
            NetworkResult.Error(exception.message ?: "Sinkronisasi gagal", exception)
        }

        return when {
            result is NetworkResult.Success -> {
                pendingActionDao.deleteById(action.id)
                Timber.d("Queue remove: id=%s type=%s", action.id, action.type)
                ActionOutcome.Done
            }
            result.shouldRetry() -> {
                pendingActionDao.update(
                    action.copy(
                        status = PendingActionStatus.FAILED,
                        retryCount = action.retryCount + 1,
                        lastError = result.errorMessage()
                    )
                )
                Timber.d("Retry scheduled: id=%s type=%s", action.id, action.type)
                ActionOutcome.RetryLater
            }
            else -> {
                pendingActionDao.update(
                    action.copy(
                        status = PendingActionStatus.FAILED,
                        retryCount = action.retryCount + 1,
                        lastError = result.errorMessage()
                    )
                )
                Timber.d("Synchronization failed without retry: id=%s type=%s", action.id, action.type)
                ActionOutcome.Done
            }
        }
    }

    private suspend fun upload(action: PendingActionEntity): NetworkResult<Unit> {
        return when (PendingActionType.valueOf(action.type)) {
            PendingActionType.ATTENDANCE_REPORT -> safeApiCall {
                apiService.submitAttendance(gson.fromJson(action.body, AttendanceReportRequest::class.java))
            } as NetworkResult<Unit>
            PendingActionType.PRODUCT_REPORT -> {
                val result = safeApiCall {
                    apiService.submitProductReport(gson.fromJson(action.body, ProductReportRequest::class.java))
                }
                when (result) {
                    is NetworkResult.Success -> NetworkResult.Success(Unit)
                    is NetworkResult.Error -> result
                    is NetworkResult.Unauthorized -> result
                    is NetworkResult.ValidationError -> result
                    NetworkResult.Loading -> NetworkResult.Loading
                }
            }
            PendingActionType.PROMO_REPORT -> safeApiCall {
                apiService.submitPromoReport(gson.fromJson(action.body, PromoReportRequest::class.java))
            } as NetworkResult<Unit>
            PendingActionType.CREATE_STORE -> {
                val request = gson.fromJson(action.body, PendingCreateStoreRequest::class.java)
                val result = safeApiCall {
                    apiService.createStore(request.store)
                }
                when (result) {
                    is NetworkResult.Success -> {
                        storeDao.deleteById(request.localId)
                        storeDao.upsert(result.data.toEntity())
                        NetworkResult.Success(Unit)
                    }
                    is NetworkResult.Error -> result
                    is NetworkResult.Unauthorized -> result
                    is NetworkResult.ValidationError -> result
                    NetworkResult.Loading -> NetworkResult.Loading
                }
            }
            PendingActionType.CREATE_AND_ASSIGN_PRODUCT -> {
                val request = gson.fromJson(action.body, CreateAndAssignProductRequest::class.java)
                val createResult = safeApiCall { apiService.createProduct(request.product) }
                when (createResult) {
                    is NetworkResult.Success -> {
                        val product = createResult.data
                        val assignResult = safeApiCall {
                            apiService.assignProductsToStore(
                                request.storeId,
                                AssignProductRequest(listOf(product.id))
                            )
                        }
                        when (assignResult) {
                            is NetworkResult.Success -> {
                                request.localProductId?.let { productDao.deleteById(request.storeId, it) }
                                productDao.upsert(product.toEntity(request.storeId))
                                NetworkResult.Success(Unit)
                            }
                            is NetworkResult.Error -> assignResult
                            is NetworkResult.Unauthorized -> assignResult
                            is NetworkResult.ValidationError -> assignResult
                            NetworkResult.Loading -> NetworkResult.Loading
                        }
                    }
                    is NetworkResult.Error -> createResult
                    is NetworkResult.Unauthorized -> createResult
                    is NetworkResult.ValidationError -> createResult
                    NetworkResult.Loading -> NetworkResult.Loading
                }
            }
            PendingActionType.ASSIGN_PRODUCT -> {
                val request = gson.fromJson(action.body, PendingAssignProductRequest::class.java)
                safeApiCall {
                    apiService.assignProductsToStore(
                        request.storeId,
                        AssignProductRequest(listOf(request.productId))
                    )
                } as NetworkResult<Unit>
            }
        }
    }

    private fun NetworkResult<*>.shouldRetry(): Boolean {
        return this is NetworkResult.Error && (code == null || code >= 500)
    }

    private fun NetworkResult<*>.errorMessage(): String {
        return when (this) {
            is NetworkResult.Error -> message
            is NetworkResult.Unauthorized -> message
            is NetworkResult.ValidationError -> message
            is NetworkResult.Success -> "Sukses"
            NetworkResult.Loading -> "Sinkronisasi berlangsung"
        }
    }
}

enum class SyncOutcome {
    Success,
    Retry
}

private enum class ActionOutcome {
    Done,
    RetryLater
}
