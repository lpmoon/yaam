package com.lpmoon.asset.data.di

import com.lpmoon.asset.data.local.AssetLocalDataSource
import com.lpmoon.asset.data.local.ExchangeRateLocalDataSource
import com.lpmoon.asset.data.local.room.AppDatabase
import com.lpmoon.asset.data.remote.ExchangeRateApiDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideAssetLocalDataSource(database: AppDatabase): AssetLocalDataSource {
        return AssetLocalDataSource(
            assetDao = database.assetDao(),
            assetHistoryDao = database.assetHistoryDao(),
            totalAssetSnapshotDao = database.totalAssetSnapshotDao()
        )
    }

    @Provides
    @Singleton
    fun provideExchangeRateLocalDataSource(database: AppDatabase): ExchangeRateLocalDataSource {
        return ExchangeRateLocalDataSource(
            exchangeRateDao = database.exchangeRateDao()
        )
    }

    @Provides
    @Singleton
    fun provideExchangeRateApiDataSource(): ExchangeRateApiDataSource {
        return ExchangeRateApiDataSource()
    }
}