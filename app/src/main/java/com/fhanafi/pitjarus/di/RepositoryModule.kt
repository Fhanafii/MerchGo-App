package com.fhanafi.pitjarus.di

import com.fhanafi.pitjarus.data.api.ApiService
import com.fhanafi.pitjarus.data.dao.ProductDao
import com.fhanafi.pitjarus.data.dao.PromoDao
import com.fhanafi.pitjarus.data.dao.StoreDao
import com.fhanafi.pitjarus.data.repository.AttendanceRepository
import com.fhanafi.pitjarus.data.repository.AuthRepository
import com.fhanafi.pitjarus.data.repository.ProductRepository
import com.fhanafi.pitjarus.data.repository.PromoRepository
import com.fhanafi.pitjarus.data.repository.StoreRepository
import com.fhanafi.pitjarus.datastore.UserPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService, userPreference: UserPreference): AuthRepository {
        return AuthRepository(apiService, userPreference)
    }

    @Provides
    @Singleton
    fun provideAttendanceRepository(apiService: ApiService, userPreference: UserPreference): AttendanceRepository {
        return AttendanceRepository(apiService, userPreference)
    }

    @Provides
    @Singleton
    fun provideStoreRepository(apiService: ApiService, storeDao: StoreDao): StoreRepository {
        return StoreRepository(apiService, storeDao)
    }

    @Provides
    @Singleton
    fun provideProductRepository(apiService: ApiService, productDao: ProductDao): ProductRepository {
        return ProductRepository(apiService, productDao)
    }

    @Provides
    @Singleton
    fun providePromoRepository(apiService: ApiService, promoDao: PromoDao): PromoRepository {
        return PromoRepository(apiService, promoDao)
    }
}
