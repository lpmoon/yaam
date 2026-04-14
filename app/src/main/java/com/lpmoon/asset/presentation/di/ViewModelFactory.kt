package com.lpmoon.asset.presentation.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lpmoon.asset.data.local.AssetLocalDataSource
import com.lpmoon.asset.data.local.ExchangeRateLocalDataSource
import com.lpmoon.asset.data.mapper.AssetMapper
import com.lpmoon.asset.data.remote.ExchangeRateApiDataSource
import com.lpmoon.asset.data.repository.AssetRepositoryImpl
import com.lpmoon.asset.data.repository.ExchangeRateRepositoryImpl
import com.lpmoon.asset.domain.usecase.*
import com.lpmoon.asset.util.AssetImportExportService
import com.lpmoon.asset.sync.AssetSyncServer
import com.lpmoon.asset.presentation.viewmodel.AssetListViewModel

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
        // 创建数据源
        val assetLocalDataSource = AssetLocalDataSource(application)
        val exchangeRateLocalDataSource = ExchangeRateLocalDataSource(application)
        val exchangeRateApiDataSource = ExchangeRateApiDataSource()
        val assetMapper = AssetMapper()

        // 创建Repository
        val assetRepository = AssetRepositoryImpl(assetLocalDataSource, assetMapper)
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
        val exportAssetsUseCase = ExportAssetsUseCase()
        val importAssetsUseCase = ImportAssetsUseCase()
        val assetImportExportService = AssetImportExportService(application)
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
            assetImportExportService = assetImportExportService,
            assetSyncServer = assetSyncServer
        )
    }
}