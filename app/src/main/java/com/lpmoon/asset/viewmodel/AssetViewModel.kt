package com.lpmoon.asset.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lpmoon.asset.data.asset.Asset
import com.lpmoon.asset.data.asset.AssetType
import com.lpmoon.asset.data.asset.CurrencyType
import com.lpmoon.asset.data.asset.ExchangeRate
import com.lpmoon.asset.data.asset.AssetRepository
import com.lpmoon.asset.data.asset.AssetHistory
import com.lpmoon.asset.data.asset.OperationType
import com.lpmoon.asset.data.asset.TotalAssetSnapshot
import com.lpmoon.asset.data.asset.TimeDimension
import com.lpmoon.asset.data.asset.AssetImportExportService
import com.lpmoon.asset.network.ExchangeRateService
import com.lpmoon.asset.util.ExpressionEvaluator
import com.lpmoon.asset.sync.AssetSyncServer
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AssetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AssetRepository(application)
    private val exchangeRateService = ExchangeRateService(application)
    private val importExportService = AssetImportExportService(application)
    private val syncServer = AssetSyncServer(application)

    private val _assets = MutableStateFlow<List<Asset>>(emptyList())
    val assets: StateFlow<List<Asset>> = _assets.asStateFlow()

    private val _exchangeRate = MutableStateFlow(ExchangeRate.getDefaultValues())
    val exchangeRate: StateFlow<ExchangeRate> = _exchangeRate.asStateFlow()

    private val _isLoadingExchangeRate = MutableStateFlow(false)
    val isLoadingExchangeRate: StateFlow<Boolean> = _isLoadingExchangeRate.asStateFlow()

    val totalAssets: StateFlow<Double> = combine(_assets, _exchangeRate) { assetsList, rate ->
        assetsList.sumOf { asset ->
            val evaluatedValue = ExpressionEvaluator.evaluate(asset.value)
            exchangeRateService.convertToCny(evaluatedValue, asset.currency, rate)
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    private var nextId: Long = 1

    init {
        loadAssets()
        loadExchangeRate()
        schedulePeriodicExchangeRateUpdate()
    }

    override fun onCleared() {
        super.onCleared()
        stopSyncServer()
    }

    private fun loadAssets() {
        val savedAssets = repository.getAllAssets()
        _assets.value = savedAssets
        if (savedAssets.isNotEmpty()) {
            nextId = (savedAssets.maxOfOrNull { it.id } ?: 0) + 1
        }
    }

    private fun loadExchangeRate() {
        viewModelScope.launch {
            _isLoadingExchangeRate.value = true
            try {
                val rate = exchangeRateService.getExchangeRate()
                _exchangeRate.value = rate
            } catch (e: Exception) {
                // 使用默认汇率
            } finally {
                _isLoadingExchangeRate.value = false
            }
        }
    }

    /**
     * 安排每小时更新汇率的定时任务
     */
    private fun schedulePeriodicExchangeRateUpdate() {
        val workManager = WorkManager.getInstance(getApplication())

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<com.lpmoon.asset.worker.ExchangeRateUpdateWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            com.lpmoon.asset.worker.ExchangeRateUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    /**
     * 强制刷新汇率
     */
    fun refreshExchangeRate() {
        viewModelScope.launch {
            _isLoadingExchangeRate.value = true
            try {
                val rate = exchangeRateService.updateExchangeRate()
                _exchangeRate.value = rate
                // 检查是否使用了默认值
                if (rate.usdToCny == 7.2 && rate.hkdToCny == 0.92) {
                    Toast.makeText(getApplication(), "汇率刷新失败，使用默认值", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(getApplication(), "汇率刷新成功", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 理论上不会走到这里，因为updateExchangeRate不抛出异常
                Toast.makeText(getApplication(), "汇率刷新失败，使用缓存数据", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoadingExchangeRate.value = false
            }
        }
    }

    fun addAsset(name: String, value: String, currency: String, type: String = AssetType.OTHER.name) {
        val newAsset = Asset(id = nextId++, name = name, value = value, currency = currency, type = type)
        _assets.value = _assets.value + newAsset
        recordHistory(newAsset.id, "", value, OperationType.CREATE.name)
        saveAssets()
        recordTotalAssetSnapshot()
    }

    fun deleteAsset(assetId: Long) {
        val oldAsset = _assets.value.find { it.id == assetId }
        _assets.value = _assets.value.filter { it.id != assetId }
        oldAsset?.let {
            // 可选：记录删除操作（注释掉以避免历史记录混淆）
            // recordHistory(assetId, it.value, "", OperationType.DELETE.name)
        }
        // 清理该资产的所有历史记录，避免ID重复使用时混淆
        repository.deleteHistoriesByAssetId(assetId)
        saveAssets()
        recordTotalAssetSnapshot()
    }

    fun updateAsset(assetId: Long, name: String, value: String, currency: String, type: String = AssetType.OTHER.name) {
        val oldAsset = _assets.value.find { it.id == assetId }
        _assets.value = _assets.value.map {
            if (it.id == assetId) {
                it.copy(name = name, value = value, currency = currency, type = type)
            } else {
                it
            }
        }
        oldAsset?.let {
            recordHistory(assetId, it.value, value, OperationType.UPDATE.name)
        }
        saveAssets()
        recordTotalAssetSnapshot()
    }

    private fun recordHistory(assetId: Long, oldValue: String, newValue: String, operationType: String) {
        val history = AssetHistory(
            id = System.currentTimeMillis(),
            assetId = assetId,
            oldValue = oldValue,
            newValue = newValue,
            timestamp = System.currentTimeMillis(),
            operationType = operationType
        )
        repository.addAssetHistory(history)
    }


    private fun recordTotalAssetSnapshot() {
        val snapshot = TotalAssetSnapshot(
            timestamp = System.currentTimeMillis(),
            totalValue = totalAssets.value
        )
        repository.addTotalAssetSnapshot(snapshot)
    }

    /**
     * 获取按时间维度聚合的总资产历史
     * @param dimension 时间维度：天、周、月、年
     * @return 列表，每个元素是时间标签和资产值的键值对
     */
    fun getTotalAssetHistory(dimension: TimeDimension): List<Pair<String, Double>> {
        val allSnapshots = repository.getAllTotalAssetHistory().sortedBy { it.timestamp }

        if (allSnapshots.isEmpty()) {
            return emptyList()
        }


        // 创建时间范围，从最早快照到当前时间
        val calendar = Calendar.getInstance(Locale.CHINA)
        // 从最早快照往前推5年，以便显示更早的历史数据
        val earliestTimestampCal = Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = allSnapshots.first().timestamp
            add(Calendar.YEAR, -5) // 往前推5年
        }
        val earliestTimestamp = earliestTimestampCal.timeInMillis
        val latestTimestamp = System.currentTimeMillis()

        // 生成时间序列键
        val timeKeys = mutableListOf<String>()
        val keyToValue = mutableMapOf<String, Double>()

        calendar.timeInMillis = earliestTimestamp
        val endCalendar = Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = latestTimestamp
        }

        // 根据维度调整时间到单位开始
        when (dimension) {
            TimeDimension.DAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeDimension.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.DAY_OF_WEEK, endCalendar.firstDayOfWeek)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeDimension.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeDimension.YEAR -> {
                calendar.set(Calendar.MONTH, 0)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.MONTH, 0)
                endCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
            else -> {} // 不会发生，但满足编译要求
        }


        // 生成所有时间键
        while (calendar.timeInMillis <= endCalendar.timeInMillis) {
            val key = when (dimension) {
                TimeDimension.DAY -> {
                    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                    sdf.format(Date(calendar.timeInMillis))
                }
                TimeDimension.WEEK -> {
                    val year = calendar.get(Calendar.YEAR)
                    val week = calendar.get(Calendar.WEEK_OF_YEAR)
                    "${year}年第${week}周"
                }
                TimeDimension.MONTH -> {
                    val sdf = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
                    sdf.format(Date(calendar.timeInMillis))
                }
                TimeDimension.YEAR -> {
                    val sdf = SimpleDateFormat("yyyy年", Locale.CHINA)
                    sdf.format(Date(calendar.timeInMillis))
                }
                else -> "" // 不会发生，但满足编译要求
            }
            timeKeys.add(key)

            // 移动到下一个时间单位
            when (dimension) {
                TimeDimension.DAY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                TimeDimension.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                TimeDimension.MONTH -> calendar.add(Calendar.MONTH, 1)
                TimeDimension.YEAR -> calendar.add(Calendar.YEAR, 1)
                else -> {} // 不会发生，但满足编译要求
            }
        }

        // 将快照分配到时间键
        val snapshotGroups = allSnapshots.groupBy { snapshot ->
            when (dimension) {
                TimeDimension.DAY -> {
                    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                    sdf.format(Date(snapshot.timestamp))
                }
                TimeDimension.WEEK -> {
                    val cal = Calendar.getInstance(Locale.CHINA).apply {
                        timeInMillis = snapshot.timestamp
                    }
                    val year = cal.get(Calendar.YEAR)
                    val week = cal.get(Calendar.WEEK_OF_YEAR)
                    "${year}年第${week}周"
                }
                TimeDimension.MONTH -> {
                    val sdf = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
                    sdf.format(Date(snapshot.timestamp))
                }
                TimeDimension.YEAR -> {
                    val sdf = SimpleDateFormat("yyyy年", Locale.CHINA)
                    sdf.format(Date(snapshot.timestamp))
                }
                else -> "" // 不会发生，但满足编译要求
            }
        }

        // 每个组取最后一个快照的值（时间戳最大的）
        snapshotGroups.forEach { (key, snapshots) ->
            val latestSnapshot = snapshots.maxByOrNull { it.timestamp }
            latestSnapshot?.let {
                keyToValue[key] = it.totalValue
            }
        }

        // 用前一个有效值填充缺失的时间键
        val result = mutableListOf<Pair<String, Double>>()
        var lastValidValue = 0.0

        for (key in timeKeys) {
            val value = keyToValue[key] ?: lastValidValue
            result.add(Pair(key, value))
            if (keyToValue[key] != null) {
                lastValidValue = value
            }
        }

        return result
    }

    /**
     * 获取资产的人民币价值
     * 如果是四则运算，先计算结果，再根据汇率转换
     */
    fun getAssetValueInCny(asset: Asset): Double {
        val evaluatedValue = ExpressionEvaluator.evaluate(asset.value)
        return exchangeRateService.convertToCny(evaluatedValue, asset.currency, _exchangeRate.value)
    }

    /**
     * 获取资产的原显示值（美化表达式）
     */
    fun getAssetDisplayValue(asset: Asset): String {
        return ExpressionEvaluator.beautify(asset.value)
    }

    /**
     * 获取资产的操作记录
     */
    fun getAssetHistory(assetId: Long): List<AssetHistory> {
        return repository.getAssetHistory(assetId)
    }

    private fun saveAssets() {
        viewModelScope.launch {
            repository.saveAssets(_assets.value)
        }
    }

    /**
     * 导出资产数据到指定的Uri（JSON文件）
     * @param uri 目标文件的Uri
     * @return 导出成功返回true，失败返回false
     */
    fun exportAssets(uri: Uri): Boolean {
        return importExportService.exportAssetsToUri(_assets.value, uri)
    }

    /**
     * 从JSON文件导入资产数据
     * @param uri JSON文件的Uri
     * @return 导入成功返回true，失败返回false
     */
    fun importAssets(uri: Uri): Boolean {
        val importedAssets = importExportService.importAssetsFromUri(uri)
        return if (importedAssets != null) {
            // 替换现有资产
            _assets.value = importedAssets
            // 更新nextId
            if (importedAssets.isNotEmpty()) {
                nextId = (importedAssets.maxOfOrNull { it.id } ?: 0) + 1
            }
            saveAssets()
            recordTotalAssetSnapshot()
            true
        } else {
            false
        }
    }

    /**
     * 生成默认的导出文件名
     * @return 默认文件名，例如：assets_export_20250410_143022.json
     */
    fun generateDefaultFileName(): String {
        return importExportService.generateDefaultFileName()
    }

    /**
     * 清空所有资产数据，包括资产列表、操作记录和 total asset snapshots
     */
    fun clearAllAssets() {
        viewModelScope.launch {
            repository.clearAllData()
            _assets.value = emptyList()
            nextId = 1
            Toast.makeText(getApplication(), "所有资产数据已清空", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 获取资产数据的 JSON 字符串表示
     * @return 资产数据的 JSON 字符串
     */
    fun getAssetsAsJson(): String {
        val exportAssets = _assets.value.map { asset ->
            AssetImportExportService.ExportAsset(
                name = asset.name,
                type = asset.type,
                value = asset.value,
                currency = asset.currency
            )
        }
        return Gson().toJson(exportAssets)
    }

    /**
     * 从 JSON 字符串导入资产数据
     * @param json JSON 字符串
     * @return 导入成功返回 true，失败返回 false
     */
    fun importFromJson(json: String): Boolean {
        return try {
            val gson = Gson()
            val type = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, AssetImportExportService.ExportAsset::class.java).type
            val exportAssets = gson.fromJson<List<AssetImportExportService.ExportAsset>>(json, type)

            val importedAssets = exportAssets.map { exportAsset ->
                Asset(
                    id = 0, // 新ID，由Repository分配
                    name = exportAsset.name,
                    value = exportAsset.value,
                    currency = exportAsset.currency ?: CurrencyType.CNY.name,
                    type = exportAsset.type ?: AssetType.OTHER.name
                )
            }

            // 替换现有资产
            _assets.value = importedAssets
            // 更新nextId
            if (importedAssets.isNotEmpty()) {
                nextId = (importedAssets.maxOfOrNull { it.id } ?: 0) + 1
            }
            saveAssets()
            recordTotalAssetSnapshot()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 启动同步服务器并生成二维码内容
     * @return 二维码内容（JSON字符串），如果启动失败返回null
     */
    fun generateSyncQrContent(): String? {
        val syncInfo = syncServer.startServer(_assets.value)
        return if (syncInfo != null) {
            val qrData = mapOf(
                "type" to "asset_sync",
                "version" to "1.0",
                "server" to syncInfo.serverAddress,
                "sessionId" to syncInfo.sessionId,
                "encryptionKey" to syncInfo.encryptionKey,
                "timestamp" to syncInfo.timestamp,
                "dataHash" to syncInfo.dataHash
            )
            Gson().toJson(qrData)
        } else {
            null
        }
    }

    /**
     * 停止同步服务器
     */
    fun stopSyncServer() {
        syncServer.stopServer()
    }

    /**
     * 检查同步服务器是否正在运行
     */
    fun isSyncServerRunning(): Boolean {
        return syncServer.isRunning()
    }
}
