package com.fhanafi.pitjarus.di

import com.fhanafi.pitjarus.data.repository.PlaceholderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun providePlaceholderRepository(): PlaceholderRepository {
        // TODO: Replace with concrete repository contracts in the next phase.
        return PlaceholderRepository()
    }
}
