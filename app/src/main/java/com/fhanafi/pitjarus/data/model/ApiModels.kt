package com.fhanafi.pitjarus.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<String>? = null,
    val pagination: PaginationDto? = null
)

data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginDto(
    val id: Int,
    val name: String,
    val token: String,
    @SerializedName("expired_at") val expiredAt: String
)

data class StoreDto(
    val id: Int,
    val code: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isActive: Boolean = true
)

data class ProductDto(
    val id: Int,
    val barcode: String,
    val name: String,
    val sku: String,
    val size: String,
    val price: Long,
    val available: Boolean? = null,
    val normalPrice: Long? = null,
    val promoPrice: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isActive: Boolean = true
)

data class AttendanceReportRequest(
    @SerializedName("client_report_id") val clientReportId: String,
    @SerializedName("attendance_type") val attendanceType: String,
    val timestamp: String,
    val location: LocationPayload,
    val photo: PhotoPayload
)

data class LocationPayload(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)

data class PhotoPayload(
    @SerializedName("file_name") val fileName: String,
    @SerializedName("mime_type") val mimeType: String,
    val base64: String
)

data class ProductReportRequest(
    @SerializedName("client_report_id") val clientReportId: String,
    @SerializedName("store_id") val storeId: Int,
    val products: List<ProductReportItem>,
    val timestamp: String
)

data class ProductReportItem(
    @SerializedName("product_id") val productId: Int,
    val available: Boolean
)

data class ProductReportDto(
    @SerializedName("report_id") val reportId: Int
)

data class PromoReportRequest(
    @SerializedName("client_report_id") val clientReportId: String,
    @SerializedName("store_id") val storeId: Int,
    val promo: List<PromoReportItem>,
    val timestamp: String
)

data class PromoReportItem(
    @SerializedName("product_name") val productName: String,
    @SerializedName("normal_price") val normalPrice: Long,
    @SerializedName("promo_price") val promoPrice: Long
)
