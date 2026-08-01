package com.fhanafi.pitjarus.data.api

import com.fhanafi.pitjarus.data.model.ApiResponse
import com.fhanafi.pitjarus.data.model.AssignProductRequest
import com.fhanafi.pitjarus.data.model.AttendanceReportRequest
import com.fhanafi.pitjarus.data.model.CreateProductRequest
import com.fhanafi.pitjarus.data.model.CreateStoreRequest
import com.fhanafi.pitjarus.data.model.LoginDto
import com.fhanafi.pitjarus.data.model.LoginRequest
import com.fhanafi.pitjarus.data.model.ProductDto
import com.fhanafi.pitjarus.data.model.ProductReportDto
import com.fhanafi.pitjarus.data.model.ProductReportRequest
import com.fhanafi.pitjarus.data.model.PromoReportRequest
import com.fhanafi.pitjarus.data.model.StoreDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("v1/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginDto>>

    @GET("v1/stores")
    suspend fun getStores(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String = ""
    ): Response<ApiResponse<List<StoreDto>>>

    @POST("v1/stores")
    suspend fun createStore(@Body request: CreateStoreRequest): Response<ApiResponse<StoreDto>>

    @GET("v1/products")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<ProductDto>>>

    @POST("v1/products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<ApiResponse<ProductDto>>

    @GET("v1/stores/{storeId}/products")
    suspend fun getStoreProducts(@Path("storeId") storeId: Int): Response<ApiResponse<List<ProductDto>>>

    @POST("v1/stores/{storeId}/products")
    suspend fun assignProductsToStore(
        @Path("storeId") storeId: Int,
        @Body request: AssignProductRequest
    ): Response<ApiResponse<Unit>>

    @POST("v1/report/attendance")
    suspend fun submitAttendance(@Body request: AttendanceReportRequest): Response<ApiResponse<Unit>>

    @POST("v1/report/product")
    suspend fun submitProductReport(@Body request: ProductReportRequest): Response<ApiResponse<ProductReportDto>>

    @POST("v1/report/promo")
    suspend fun submitPromoReport(@Body request: PromoReportRequest): Response<ApiResponse<Unit>>
}
