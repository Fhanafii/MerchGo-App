package com.fhanafi.pitjarus.data.repository

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.dao.PromoDao
import com.fhanafi.pitjarus.data.entity.PendingActionType
import com.fhanafi.pitjarus.data.entity.PromoEntity
import com.fhanafi.pitjarus.data.mapper.toUiModel
import com.fhanafi.pitjarus.data.model.PromoReportItem
import com.fhanafi.pitjarus.data.model.PromoReportRequest
import com.fhanafi.pitjarus.data.sync.PendingActionRepository
import com.fhanafi.pitjarus.ui.model.PromoUiModel
import com.fhanafi.pitjarus.utils.NetworkResult
import com.fhanafi.pitjarus.utils.isoTimestampNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class PromoRepository @Inject constructor(
    private val apiService: ApiService,
    private val promoDao: PromoDao,
    private val pendingActionRepository: PendingActionRepository
) {
    fun observePromos(storeId: Int): Flow<List<PromoUiModel>> {
        return promoDao.observePromos(storeId).map { promos -> promos.map { it.toUiModel() } }
    }

    suspend fun addPromo(storeId: Int, productName: String, normalPrice: Long, promoPrice: Long) {
        promoDao.insert(
            PromoEntity(
                storeId = storeId,
                productName = productName,
                normalPrice = normalPrice,
                promoPrice = promoPrice,
                createdAt = isoTimestampNow()
            )
        )
    }

    suspend fun submitReport(storeId: Int): NetworkResult<Unit> {
        val promos = promoDao.observePromos(storeId).first()
        val request = PromoReportRequest(
            clientReportId = UUID.randomUUID().toString(),
            storeId = storeId,
            promo = promos.map { PromoReportItem(it.productName, it.normalPrice, it.promoPrice) },
            timestamp = isoTimestampNow()
        )
        val result = safeApiCall { apiService.submitPromoReport(request) }
        if (result is NetworkResult.Success) {
            promoDao.clearByStore(storeId)
        } else if (result.isRetryableFailure()) {
            pendingActionRepository.enqueue(PendingActionType.PROMO_REPORT, request)
            promoDao.clearByStore(storeId)
            return NetworkResult.Success(Unit)
        }
        return result as NetworkResult<Unit>
    }
}
