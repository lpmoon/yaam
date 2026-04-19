package com.lpmoon.asset.presentation.di

import android.content.Context
import com.lpmoon.asset.sync.AssetSyncServer
import com.lpmoon.asset.util.FileIoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideFileIoService(@ApplicationContext context: Context): FileIoService {
        return FileIoService(context)
    }

    @Provides
    @Singleton
    fun provideAssetSyncServer(@ApplicationContext context: Context): AssetSyncServer {
        return AssetSyncServer(context)
    }
}