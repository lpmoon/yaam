package com.lpmoon.asset.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lpmoon.asset.domain.model.ExchangeRate
import com.lpmoon.asset.domain.model.TimeDimension
import com.lpmoon.asset.domain.usecase.*
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

    private val _assets = MutableStateFlow<List<com.lpmoon.asset.data.asset.Asset>>(emptyList())
    val assets: StateFlow<List<com.lpmoon.asset.data.asset.Asset>> = _assets.asStateFlow()

    private val _exchangeRate = MutableStateFlow(com.lpmoon.asset.data.asset.ExchangeRate.getDefaultValues())
    val exchangeRate: StateFlow<com.lpmoon.asset.data.asset.ExchangeRate> = _exchangeRate.asStateFlow()

    private val _isLoadingExchangeRate = MutableStateFlow(false)
    val isLoadingExchangeRate: StateFlow<Boolean> = _isLoadingExchangeRate.asStateFlow()

    private val _totalAssets = MutableStateFlow(0.0)
    val totalAssets: StateFlow<Double> = _totalAssets.asStateFlow()

    // 简单的转换函数（因为数据层和领域层模型结构相同）
    private fun com.lpmoon.asset.data.asset.Asset.toDomain(): com.lpmoon.asset.domain.model.Asset {
        return com.lpmoon.asset.domain.model.Asset(
            id = this.id,
            name = this.name,
            value = this.value,
            currency = this.currency,
            type = this.type
        )
    }

    private fun com.lpmoon.asset.domain.model.Asset.toData(): com.lpmoon.asset.data.asset.Asset {
        return com.lpmoon.asset.data.asset.Asset(
            id = this.id,
            name = this.name,
            value = this.value,
            currency = this.currency,
            type = this.type
        )
    }

    // 初始化加载数据
    init {
        loadAssets()
        loadExchangeRate()
        setupTotalAssetsFlow()
    }

    private fun setupTotalAssetsFlow() {
        viewModelScope.launch {
            calculateTotalAssetsUseCase().collect { total ->
                _totalAssets.value = total
            }
        }
    }

    private fun loadAssets() {
        viewModelScope.launch {
            getAllAssetsUseCase().collect { domainAssets ->
                // 将领域层资产转换为数据层资产
                val dataAssets = domainAssets.map { it.toData() }
                _assets.value = dataAssets
            }
        }
    }

    private fun loadExchangeRate() {
        viewModelScope.launch {
            // 初始加载汇率
            try {
                val domainRate = getExchangeRateUseCase()
                val dataRate = com.lpmoon.asset.data.asset.ExchangeRate(
                    usdToCny = domainRate.usdToCny,
                    hkdToCny = domainRate.hkdToCny,
                    lastUpdateTime = domainRate.lastUpdateTime
                )
                _exchangeRate.value = dataRate
            } catch (e: Exception) {
                // 使用默认值
                _exchangeRate.value = com.lpmoon.asset.data.asset.ExchangeRate.getDefaultValues()
            }

            // 可以定期刷新汇率，但暂时由refreshExchangeRate方法处理
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
            // 计算当前总资产并添加快照
            try {
                val currentTotal = calculateTotalAssetsUseCase().first()
                addTotalAssetSnapshotUseCase(currentTotal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            // 计算当前总资产并添加快照
            try {
                val currentTotal = calculateTotalAssetsUseCase().first()
                addTotalAssetSnapshotUseCase(currentTotal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAsset(assetId: Long) {
        viewModelScope.launch {
            deleteAssetUseCase(assetId)
            // 计算当前总资产并添加快照
            try {
                val currentTotal = calculateTotalAssetsUseCase().first()
                addTotalAssetSnapshotUseCase(currentTotal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAssetHistory(assetId: Long): List<com.lpmoon.asset.data.asset.AssetHistory> {
        // 在后台协程中获取资产历史
        return kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val domainHistories = getAssetHistoryUseCase(assetId).first()
                domainHistories.map { domainHistory ->
                    com.lpmoon.asset.data.asset.AssetHistory(
                        id = domainHistory.id,
                        assetId = domainHistory.assetId,
                        oldValue = domainHistory.oldValue,
                        newValue = domainHistory.newValue,
                        timestamp = domainHistory.timestamp,
                        operationType = domainHistory.operationType
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getTotalAssetHistory(dimension: com.lpmoon.asset.data.asset.TimeDimension): List<Pair<String, Double>> {
        // 在后台协程中计算总资产历史
        return kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val domainDimension = when (dimension) {
                    com.lpmoon.asset.data.asset.TimeDimension.DAY -> com.lpmoon.asset.domain.model.TimeDimension.DAY
                    com.lpmoon.asset.data.asset.TimeDimension.WEEK -> com.lpmoon.asset.domain.model.TimeDimension.WEEK
                    com.lpmoon.asset.data.asset.TimeDimension.MONTH -> com.lpmoon.asset.domain.model.TimeDimension.MONTH
                    com.lpmoon.asset.data.asset.TimeDimension.YEAR -> com.lpmoon.asset.domain.model.TimeDimension.YEAR
                }
                calculateAssetHistoryUseCase(domainDimension)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getAssetValueInCny(asset: com.lpmoon.asset.data.asset.Asset): Double {
        val domainAsset = asset.toDomain()
        val evaluatedValue = ExpressionEvaluator.evaluate(domainAsset.value)
        return convertCurrency(evaluatedValue, domainAsset.currency, exchangeRate.value)
    }

    private fun convertCurrency(amount: Double, currency: String, exchangeRate: com.lpmoon.asset.data.asset.ExchangeRate): Double {
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
            refreshExchangeRateUseCase()
        }
    }

    fun getAssetDisplayValue(asset: com.lpmoon.asset.data.asset.Asset): String {
        val domainAsset = asset.toDomain()
        return ExpressionEvaluator.beautify(domainAsset.value)
    }

    fun exportAssets(uri: android.net.Uri): Boolean {
        return kotlinx.coroutines.runBlocking {
            try {
                // 将数据层资产转换为领域层资产
                val domainAssets = _assets.value.map { it.toDomain() }

                val exportResult = exportAssetsUseCase(
                    FileExportAssetsUseCase.Params(
                        assets = domainAssets,
                        exportInfo = FileExportAssetsUseCase.ExportInfo(
                            fileName = null // 使用默认文件名
                        )
                    )
                )
                if (exportResult.success && exportResult.exportData != null) {
                    // 使用FileIoService将数据写入Uri（平台特定操作）
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
                // 使用FileIoService读取JSON字符串（平台特定操作）
                val importedDataJson = fileIoService.readJsonFromUri(uri)
                if (importedDataJson == null) {
                    return@runBlocking false
                }

                // 使用ImportAssetsUseCase处理导入逻辑（业务逻辑）
                val importResult = importAssetsUseCase(
                    FileImportAssetsUseCase.Params(
                        importData = importedDataJson,
                        importInfo = FileImportAssetsUseCase.ImportInfo()
                    )
                )

                return@runBlocking if (importResult.success && importResult.importedAssets != null) {
                    handleImportedAssets(importResult.importedAssets)
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

    private fun handleImportedAssets(domainAssets: List<com.lpmoon.asset.domain.model.Asset>) {
        // 转换为数据层资产并更新UI状态
        val dataAssets = domainAssets.map { it.toData() }
        _assets.value = dataAssets

        viewModelScope.launch {
            // 保存到Repository
            saveAssetsUseCase(domainAssets)

            // 计算当前总资产并添加快照
            try {
                val currentTotal = calculateTotalAssetsUseCase().first()
                addTotalAssetSnapshotUseCase(currentTotal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun generateDefaultFileName(): String {
        return fileIoService.generateDefaultFileName()
    }

    fun clearAllAssets() {
        viewModelScope.launch {
            clearAllAssetsUseCase()
            _assets.value = emptyList()
            // 添加快照（总资产为0）
            try {
                addTotalAssetSnapshotUseCase(0.0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAssetsAsJson(): String {
        val gson = com.google.gson.Gson()
        return gson.toJson(_assets.value)
    }

    fun importFromJson(json: String): Boolean {
        return kotlinx.coroutines.runBlocking {
            try {
                // 使用ImportAssetsUseCase处理导入逻辑（业务逻辑）
                val importResult = importAssetsUseCase(
                    FileImportAssetsUseCase.Params(
                        importData = json,
                        importInfo = FileImportAssetsUseCase.ImportInfo()
                    )
                )

                return@runBlocking if (importResult.success && importResult.importedAssets != null) {
                    handleImportedAssets(importResult.importedAssets)
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
        // 将数据层资产转换为领域层资产
        val domainAssets = _assets.value.map { it.toDomain() }

        val snapshotResult = generateAssetSnapshotUseCase(
            GenerateAssetSnapshotUseCase.Params(
                context = context,
                assets = domainAssets,
                totalAssets = _totalAssets.value,
                getAssetValueInCny = { asset ->
                    // 使用ViewModel中的转换逻辑
                    val evaluatedValue = com.lpmoon.asset.util.ExpressionEvaluator.evaluate(asset.value)
                    convertCurrency(evaluatedValue, asset.currency, exchangeRate.value)
                }
            )
        )

        return if (snapshotResult.success && snapshotResult.bitmap != null) {
            // 使用FileIoService保存位图到图库
            fileIoService.saveBitmapToGallery(snapshotResult.bitmap)
        } else {
            false
        }
    }

    fun generateSyncQrContent(): String? {
        val syncInfo = assetSyncServer.startServer(_assets.value)
        return syncInfo?.let { info ->
            // 创建QR码数据，包含服务器信息
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