package com.dev.headphoneplayer.di

import com.dev.headphoneplayer.data.repository.HeadphonePlayerRepositoryImpl
import com.dev.headphoneplayer.domain.repository.HeadphonePlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {


    @Binds
    @Singleton
    abstract fun bindHeadphonePlayerRepository(
        repository: HeadphonePlayerRepositoryImpl
    ): HeadphonePlayerRepository


}