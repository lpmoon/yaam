package com.lpmoon.asset.data.di

import com.lpmoon.asset.data.local.AssetLocalDataSource
import com.lpmoon.asset.data.local.ExchangeRateLocalDataSource
import com.lpmoon.asset.data.remote.ExchangeRateApiDataSource
import com.lpmoon.asset.data.repository.AssetRepositoryImpl
import com.lpmoon.asset.data.repository.ExchangeRateRepositoryImpl
import com.lpmoon.asset.domain.repository.AssetRepository
import com.lpmoon.asset.domain.repository.ExchangeRateRepository
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
    fun provideAssetRepository(
        assetLocalDataSource: AssetLocalDataSource
    ): AssetRepository {
        return AssetRepositoryImpl(assetLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideExchangeRateRepository(
        exchangeRateLocalDataSource: ExchangeRateLocalDataSource,
        exchangeRateApiDataSource: ExchangeRateApiDataSource
    ): ExchangeRateRepository {
        return ExchangeRateRepositoryImpl(
            localDataSource = exchangeRateLocalDataSource,
            apiDataSource = exchangeRateApiDataSource
        )
    }
}