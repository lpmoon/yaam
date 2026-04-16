package com.lpmoon.asset.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.ExchangeRate
import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.model.asset.TimeDimension
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
import com.lpmoon.asset.util.ExpressionEvaluator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 资产列表ViewModel（精简版）
 * 只负责UI状态管理，业务逻辑委托给UseCase
 */
class AssetListViewModel(
    application: Application,
    private val addAssetUseCase: AddAssetUseCase,
    private val updateAssetUseCase: UpdateAssetUseCase,
    private val deleteAssetUseCase: DeleteAssetUseCase,
    private val getAssetHistoryUseCase: GetAssetHistoryUseCase,
    private val calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase,
    private val calculateAssetHistoryUseCase: CalculateAssetHistoryUseCase,
    private val refreshExchangeRateUseCase: RefreshExchangeRateUseCase,
    private val getAllAssetsUseCase: GetAllAssetsUseCase,
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val clearAllAssetsUseCase: ClearAllAssetsUseCase,
    private val saveAssetsUseCase: SaveAssetsUseCase,
    private val exportAssetsUseCase: FileExportAssetsUseCase,
    private val importAssetsUseCase: FileImportAssetsUseCase,
    private val generateAssetSnapshotUseCase: GenerateAssetSnapshotUseCase,
    private val addTotalAssetSnapshotUseCase: AddTotalAssetSnapshotUseCase,
    private val fileIoService: com.lpmoon.asset.util.FileIoService,
    private val assetSyncServer: com.lpmoon.asset.sync.AssetSyncServer
) : AndroidViewModel(application) {

    val assets: StateFlow<List<Asset>> = getAllAssetsUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _exchangeRate = MutableStateFlow(ExchangeRate.getDefaultValues())
    val exchangeRate: StateFlow<ExchangeRate> = _exchangeRate.asStateFlow()

    private val _isLoadingExchangeRate = MutableStateFlow(false)
    val isLoadingExchangeRate: StateFlow<Boolean> = _isLoadingExchangeRate.asStateFlow()

    val totalAssets: StateFlow<Double> = calculateTotalAssetsUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    init {
        loadExchangeRate()
    }

    private fun loadExchangeRate() {
        viewModelScope.launch {
            try {
                _exchangeRate.value = getExchangeRateUseCase()
            } catch (e: Exception) {
                _exchangeRate.value = ExchangeRate.getDefaultValues()
            }
        }
    }

    fun addAsset(name: String, value: String, currency: String, type: String) {
        viewModelScope.launch {
            addAssetUseCase(
                AddAssetUseCase.Params(
                    name = name,
                    value = value,
                    currency = currency,
                    type = type
                )
            )
            addTotalAssetSnapshotIfNeeded()
        }
    }

    fun updateAsset(assetId: Long, name: String, value: String, currency: String, type: String) {
        viewModelScope.launch {
            updateAssetUseCase(
                UpdateAssetUseCase.Params(
                    assetId = assetId,
                    name = name,
                    value = value,
                    currency = currency,
                    type = type
                )
            )
            addTotalAssetSnapshotIfNeeded()
        }
    }

    fun deleteAsset(assetId: Long) {
        viewModelScope.launch {
            deleteAssetUseCase(assetId)
            addTotalAssetSnapshotIfNeeded()
        }
    }

    private suspend fun addTotalAssetSnapshotIfNeeded() {
        try {
            val currentTotal = calculateTotalAssetsUseCase().first()
            addTotalAssetSnapshotUseCase(currentTotal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAssetHistory(assetId: Long): List<AssetHistory> {
        return kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            try {
                getAssetHistoryUseCase(assetId).first()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getTotalAssetHistory(dimension: TimeDimension): List<Pair<String, Double>> {
        return kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            try {
                calculateAssetHistoryUseCase(dimension)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getAssetValueInCny(asset: Asset): Double {
        val evaluatedValue = ExpressionEvaluator.evaluate(asset.value)
        return convertCurrency(evaluatedValue, asset.currency, exchangeRate.value)
    }

    private fun convertCurrency(amount: Double, currency: String, exchangeRate: ExchangeRate): Double {
        val currencyType = if (currency.isBlank()) "CNY" else currency
        return when (currencyType) {
            "CNY" -> amount
            "USD" -> amount * exchangeRate.usdToCny
            "HKD" -> amount * exchangeRate.hkdToCny
            else -> amount
        }
    }

    fun refreshExchangeRate() {
        viewModelScope.launch {
            _isLoadingExchangeRate.value = true
            try {
                refreshExchangeRateUseCase()
                _exchangeRate.value = getExchangeRateUseCase()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingExchangeRate.value = false
            }
        }
    }

    fun getAssetDisplayValue(asset: Asset): String {
        return ExpressionEvaluator.beautify(asset.value)
    }

    fun exportAssets(uri: android.net.Uri): Boolean {
        return kotlinx.coroutines.runBlocking {
            try {
                val exportResult = exportAssetsUseCase(
                    FileExportAssetsUseCase.Params(
                        assets = assets.value,
                        exportInfo = FileExportAssetsUseCase.ExportInfo(
                            fileName = null
                        )
                    )
                )
                if (exportResult.success && exportResult.exportData != null) {
                    fileIoService.writeJsonToUri(exportResult.exportData.data, uri)
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun importAssets(uri: android.net.Uri): Boolean {
        return kotlinx.coroutines.runBlocking {
            try {
                val importedDataJson = fileIoService.readJsonFromUri(uri) ?: return@runBlocking false

                val importResult = importAssetsUseCase(
                    FileImportAssetsUseCase.Params(
                        importData = importedDataJson,
                        importInfo = FileImportAssetsUseCase.ImportInfo()
                    )
                )

                return@runBlocking if (importResult.success && importResult.importedAssets != null) {
                    saveAssetsUseCase(importResult.importedAssets)
                    addTotalAssetSnapshotIfNeeded()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun generateDefaultFileName(): String {
        return fileIoService.generateDefaultFileName()
    }

    fun clearAllAssets() {
        viewModelScope.launch {
            clearAllAssetsUseCase()
            try {
                addTotalAssetSnapshotUseCase(0.0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAssetsAsJson(): String {
        val gson = com.google.gson.Gson()
        return gson.toJson(assets.value)
    }

    fun importFromJson(json: String): Boolean {
        return kotlinx.coroutines.runBlocking {
            try {
                val importResult = importAssetsUseCase(
                    FileImportAssetsUseCase.Params(
                        importData = json,
                        importInfo = FileImportAssetsUseCase.ImportInfo()
                    )
                )

                return@runBlocking if (importResult.success && importResult.importedAssets != null) {
                    saveAssetsUseCase(importResult.importedAssets)
                    addTotalAssetSnapshotIfNeeded()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun generateAssetSnapshot(context: android.content.Context): Boolean {
        val snapshotResult = generateAssetSnapshotUseCase(
            GenerateAssetSnapshotUseCase.Params(
                context = context,
                assets = assets.value,
                totalAssets = totalAssets.value,
                getAssetValueInCny = { asset ->
                    val evaluatedValue = ExpressionEvaluator.evaluate(asset.value)
                    convertCurrency(evaluatedValue, asset.currency, exchangeRate.value)
                }
            )
        )

        return if (snapshotResult.success && snapshotResult.bitmap != null) {
            fileIoService.saveBitmapToGallery(snapshotResult.bitmap)
        } else {
            false
        }
    }

    fun generateSyncQrContent(): String? {
        val syncInfo = assetSyncServer.startServer(assets.value)
        return syncInfo?.let { info ->
            val qrData = mapOf(
                "type" to "asset_sync",
                "version" to "1.0",
                "serverAddress" to info.serverAddress,
                "sessionId" to info.sessionId,
                "encryptionKey" to info.encryptionKey,
                "timestamp" to info.timestamp.toString(),
                "dataHash" to info.dataHash
            )
            com.google.gson.Gson().toJson(qrData)
        }
    }

    fun stopSyncServer() {
        assetSyncServer.stopServer()
    }
}
