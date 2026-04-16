package com.lpmoon.asset.presentation.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lpmoon.asset.data.local.AssetLocalDataSource
import com.lpmoon.asset.data.local.ExchangeRateLocalDataSource
import com.lpmoon.asset.data.local.room.AppDatabase
import com.lpmoon.asset.data.remote.ExchangeRateApiDataSource
import com.lpmoon.asset.data.repository.AssetRepositoryImpl
import com.lpmoon.asset.data.repository.ExchangeRateRepositoryImpl
import com.lpmoon.asset.domain.usecase.asset.AddAssetUseCase
import com.lpmoon.asset.domain.usecase.asset.AddTotalAssetSnapshotUseCase
import com.lpmoon.asset.domain.usecase.asset.CalculateAssetHistoryUseCase
import com.lpmoon.asset.domain.usecase.asset.CalculateTotalAssetsUseCase
import com.lpmoon.asset.domain.usecase.asset.ClearAllAssetsUseCase
import com.lpmoon.asset.domain.usecase.asset.DeleteAssetUseCase
import com.lpmoon.asset.domain.usecase.asset.FileExportAssetsUseCase
import com.lpmoon.asset.domain.usecase.asset.FileImportAssetsUseCase
import com.lpmoon.asset.domain.usecase.asset.GenerateAssetSnapshotUseCase
import com.lpmoon.asset.domain.usecase.asset.GetAllAssetsUseCase
import com.lpmoon.asset.domain.usecase.asset.GetAssetHistoryUseCase
import com.lpmoon.asset.domain.usecase.asset.GetExchangeRateUseCase
import com.lpmoon.asset.domain.usecase.asset.RefreshExchangeRateUseCase
import com.lpmoon.asset.domain.usecase.asset.SaveAssetsUseCase
import com.lpmoon.asset.domain.usecase.asset.UpdateAssetUseCase
import com.lpmoon.asset.presentation.viewmodel.AssetListViewModel
import com.lpmoon.asset.sync.AssetSyncServer
import com.lpmoon.asset.util.FileIoService

/**
 * ViewModel工厂，用于手动构造依赖
 */
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetListViewModel::class.java)) {
            return createAssetListViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    private fun createAssetListViewModel(): AssetListViewModel {
        // 创建 Room 数据库实例
        val database = AppDatabase.getInstance(application)

        // 创建数据源（Room 实现）
        val assetLocalDataSource = AssetLocalDataSource(
            assetDao = database.assetDao(),
            assetHistoryDao = database.assetHistoryDao(),
            totalAssetSnapshotDao = database.totalAssetSnapshotDao()
        )
        val exchangeRateLocalDataSource = ExchangeRateLocalDataSource(
            exchangeRateDao = database.exchangeRateDao()
        )
        val exchangeRateApiDataSource = ExchangeRateApiDataSource()

        // 创建Repository
        val assetRepository = AssetRepositoryImpl(assetLocalDataSource)
        val exchangeRateRepository = ExchangeRateRepositoryImpl(
            exchangeRateLocalDataSource,
            exchangeRateApiDataSource
        )

        // 创建UseCase
        val calculateTotalAssetsUseCase = CalculateTotalAssetsUseCase(
            assetRepository,
            exchangeRateRepository
        )
        val calculateAssetHistoryUseCase = CalculateAssetHistoryUseCase(assetRepository)
        val addAssetUseCase = AddAssetUseCase(assetRepository)
        val updateAssetUseCase = UpdateAssetUseCase(assetRepository)
        val deleteAssetUseCase = DeleteAssetUseCase(assetRepository)
        val getAssetHistoryUseCase = GetAssetHistoryUseCase(assetRepository)
        val refreshExchangeRateUseCase = RefreshExchangeRateUseCase(exchangeRateRepository)
        val getAllAssetsUseCase = GetAllAssetsUseCase(assetRepository)
        val getExchangeRateUseCase = GetExchangeRateUseCase(exchangeRateRepository)
        val clearAllAssetsUseCase = ClearAllAssetsUseCase(assetRepository)
        val saveAssetsUseCase = SaveAssetsUseCase(assetRepository)
        val exportAssetsUseCase = FileExportAssetsUseCase()
        val importAssetsUseCase = FileImportAssetsUseCase()
        val generateAssetSnapshotUseCase = GenerateAssetSnapshotUseCase()
        val addTotalAssetSnapshotUseCase = AddTotalAssetSnapshotUseCase(assetRepository)
        val fileIoService = FileIoService(application)
        val assetSyncServer = AssetSyncServer(application)

        return AssetListViewModel(
            application = application,
            addAssetUseCase = addAssetUseCase,
            updateAssetUseCase = updateAssetUseCase,
            deleteAssetUseCase = deleteAssetUseCase,
            getAssetHistoryUseCase = getAssetHistoryUseCase,
            calculateTotalAssetsUseCase = calculateTotalAssetsUseCase,
            calculateAssetHistoryUseCase = calculateAssetHistoryUseCase,
            refreshExchangeRateUseCase = refreshExchangeRateUseCase,
            getAllAssetsUseCase = getAllAssetsUseCase,
            getExchangeRateUseCase = getExchangeRateUseCase,
            clearAllAssetsUseCase = clearAllAssetsUseCase,
            saveAssetsUseCase = saveAssetsUseCase,
            exportAssetsUseCase = exportAssetsUseCase,
            importAssetsUseCase = importAssetsUseCase,
            generateAssetSnapshotUseCase = generateAssetSnapshotUseCase,
            addTotalAssetSnapshotUseCase = addTotalAssetSnapshotUseCase,
            fileIoService = fileIoService,
            assetSyncServer = assetSyncServer
        )
    }
}