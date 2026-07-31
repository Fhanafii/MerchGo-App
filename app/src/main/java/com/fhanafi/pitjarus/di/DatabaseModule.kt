package com.fhanafi.pitjarus.di

import android.content.Context
import androidx.room.Room
import com.fhanafi.pitjarus.data.dao.PendingActionDao
import com.fhanafi.pitjarus.data.dao.ProductDao
import com.fhanafi.pitjarus.data.dao.PromoDao
import com.fhanafi.pitjarus.data.dao.StoreDao
import com.fhanafi.pitjarus.data.database.AppDatabase
import com.fhanafi.pitjarus.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStoreDao(database: AppDatabase): StoreDao {
        return database.storeDao()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun providePromoDao(database: AppDatabase): PromoDao {
        return database.promoDao()
    }

    @Provides
    fun providePendingActionDao(database: AppDatabase): PendingActionDao {
        return database.pendingActionDao()
    }
}
