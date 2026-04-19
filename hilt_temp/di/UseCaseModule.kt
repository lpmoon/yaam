package com.lpmoon.asset.domain.di

import com.lpmoon.asset.domain.repository.AssetRepository
import com.lpmoon.asset.domain.repository.ExchangeRateRepository
import com.lpmoon.asset.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideAddAssetUseCase(assetRepository: AssetRepository): AddAssetUseCase {
        return AddAssetUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateAssetUseCase(assetRepository: AssetRepository): UpdateAssetUseCase {
        return UpdateAssetUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteAssetUseCase(assetRepository: AssetRepository): DeleteAssetUseCase {
        return DeleteAssetUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideGetAssetHistoryUseCase(assetRepository: AssetRepository): GetAssetHistoryUseCase {
        return GetAssetHistoryUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideCalculateTotalAssetsUseCase(
        assetRepository: AssetRepository,
        exchangeRateRepository: ExchangeRateRepository
    ): CalculateTotalAssetsUseCase {
        return CalculateTotalAssetsUseCase(assetRepository, exchangeRateRepository)
    }

    @Provides
    @Singleton
    fun provideCalculateAssetHistoryUseCase(
        assetRepository: AssetRepository
    ): CalculateAssetHistoryUseCase {
        return CalculateAssetHistoryUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideRefreshExchangeRateUseCase(
        exchangeRateRepository: ExchangeRateRepository
    ): RefreshExchangeRateUseCase {
        return RefreshExchangeRateUseCase(exchangeRateRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllAssetsUseCase(
        assetRepository: AssetRepository
    ): GetAllAssetsUseCase {
        return GetAllAssetsUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideGetExchangeRateUseCase(
        exchangeRateRepository: ExchangeRateRepository
    ): GetExchangeRateUseCase {
        return GetExchangeRateUseCase(exchangeRateRepository)
    }

    @Provides
    @Singleton
    fun provideClearAllAssetsUseCase(
        assetRepository: AssetRepository
    ): ClearAllAssetsUseCase {
        return ClearAllAssetsUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideSaveAssetsUseCase(
        assetRepository: AssetRepository
    ): SaveAssetsUseCase {
        return SaveAssetsUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideFileExportAssetsUseCase(): FileExportAssetsUseCase {
        return FileExportAssetsUseCase()
    }

    @Provides
    @Singleton
    fun provideFileImportAssetsUseCase(): FileImportAssetsUseCase {
        return FileImportAssetsUseCase()
    }

    @Provides
    @Singleton
    fun provideGenerateAssetSnapshotUseCase(): GenerateAssetSnapshotUseCase {
        return GenerateAssetSnapshotUseCase()
    }

    @Provides
    @Singleton
    fun provideAddTotalAssetSnapshotUseCase(
        assetRepository: AssetRepository
    ): AddTotalAssetSnapshotUseCase {
        return AddTotalAssetSnapshotUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideQRExportAssetsUseCase(): QRExportAssetsUseCase {
        return QRExportAssetsUseCase()
    }

    @Provides
    @Singleton
    fun provideQRImportAssetsUseCase(): QRImportAssetsUseCase {
        return QRImportAssetsUseCase()
    }

    @Provides
    @Singleton
    fun provideConvertCurrencyUseCase(): ConvertCurrencyUseCase {
        return ConvertCurrencyUseCase()
    }

    @Provides
    @Singleton
    fun provideEvaluateExpressionUseCase(): EvaluateExpressionUseCase {
        return EvaluateExpressionUseCase()
    }

    @Provides
    @Singleton
    fun provideAssetUseCases(
        addAssetUseCase: AddAssetUseCase,
        updateAssetUseCase: UpdateAssetUseCase,
        deleteAssetUseCase: DeleteAssetUseCase,
        getAssetHistoryUseCase: GetAssetHistoryUseCase,
        calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase,
        calculateAssetHistoryUseCase: CalculateAssetHistoryUseCase,
        getAllAssetsUseCase: GetAllAssetsUseCase,
        clearAllAssetsUseCase: ClearAllAssetsUseCase,
        saveAssetsUseCase: SaveAssetsUseCase,
        addTotalAssetSnapshotUseCase: AddTotalAssetSnapshotUseCase
    ): AssetUseCases {
        return AssetUseCases(
            add = addAssetUseCase,
            update = updateAssetUseCase,
            delete = deleteAssetUseCase,
            getHistory = getAssetHistoryUseCase,
            calculateTotal = calculateTotalAssetsUseCase,
            calculateHistory = calculateAssetHistoryUseCase,
            getAll = getAllAssetsUseCase,
            clearAll = clearAllAssetsUseCase,
            save = saveAssetsUseCase,
            addTotalSnapshot = addTotalAssetSnapshotUseCase
        )
    }

    @Provides
    @Singleton
    fun provideExchangeRateUseCases(
        refreshExchangeRateUseCase: RefreshExchangeRateUseCase,
        getExchangeRateUseCase: GetExchangeRateUseCase
    ): ExchangeRateUseCases {
        return ExchangeRateUseCases(
            refresh = refreshExchangeRateUseCase,
            get = getExchangeRateUseCase
        )
    }

    @Provides
    @Singleton
    fun provideFileUseCases(
        fileExportAssetsUseCase: FileExportAssetsUseCase,
        fileImportAssetsUseCase: FileImportAssetsUseCase
    ): FileUseCases {
        return FileUseCases(
            export = fileExportAssetsUseCase,
            import = fileImportAssetsUseCase
        )
    }

    @Provides
    @Singleton
    fun provideOtherUseCases(
        generateAssetSnapshotUseCase: GenerateAssetSnapshotUseCase,
        qrExportAssetsUseCase: QRExportAssetsUseCase,
        qrImportAssetsUseCase: QRImportAssetsUseCase,
        convertCurrencyUseCase: ConvertCurrencyUseCase,
        evaluateExpressionUseCase: EvaluateExpressionUseCase
    ): OtherUseCases {
        return OtherUseCases(
            generateSnapshot = generateAssetSnapshotUseCase,
            qrExport = qrExportAssetsUseCase,
            qrImport = qrImportAssetsUseCase,
            convertCurrency = convertCurrencyUseCase,
            evaluateExpression = evaluateExpressionUseCase
        )
    }
}