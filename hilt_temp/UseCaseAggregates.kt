package com.lpmoon.asset.domain.usecase

/**
 * 资产相关的UseCase聚合类
 * 用于减少ViewModel构造函数参数数量
 */
data class AssetUseCases(
    val add: AddAssetUseCase,
    val update: UpdateAssetUseCase,
    val delete: DeleteAssetUseCase,
    val getHistory: GetAssetHistoryUseCase,
    val calculateTotal: CalculateTotalAssetsUseCase,
    val calculateHistory: CalculateAssetHistoryUseCase,
    val getAll: GetAllAssetsUseCase,
    val clearAll: ClearAllAssetsUseCase,
    val save: SaveAssetsUseCase,
    val addTotalSnapshot: AddTotalAssetSnapshotUseCase
)

/**
 * 汇率相关的UseCase聚合类
 */
data class ExchangeRateUseCases(
    val refresh: RefreshExchangeRateUseCase,
    val get: GetExchangeRateUseCase
)

/**
 * 文件相关的UseCase聚合类
 */
data class FileUseCases(
    val export: FileExportAssetsUseCase,
    val import: FileImportAssetsUseCase
)

/**
 * 其他UseCase聚合类
 */
data class OtherUseCases(
    val generateSnapshot: GenerateAssetSnapshotUseCase,
    val qrExport: QRExportAssetsUseCase,
    val qrImport: QRImportAssetsUseCase,
    val convertCurrency: ConvertCurrencyUseCase,
    val evaluateExpression: EvaluateExpressionUseCase
)