package com.lpmoon.asset.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lpmoon.asset.data.asset.Asset as DataAsset
import com.lpmoon.asset.data.asset.AssetHistory as DataAssetHistory
import com.lpmoon.asset.data.asset.TotalAssetSnapshot as DataTotalAssetSnapshot
import com.lpmoon.asset.data.mapper.AssetMapper
import com.lpmoon.asset.domain.model.Asset as DomainAsset
import com.lpmoon.asset.domain.model.AssetHistory as DomainAssetHistory
import com.lpmoon.asset.domain.model.TotalAssetSnapshot as DomainTotalAssetSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 资产本地数据源
 * 负责使用SharedPreferences存储和检索资产数据
 */
class AssetLocalDataSource(
    private val context: Context,
    private val gson: Gson = Gson(),
    private val mapper: AssetMapper = AssetMapper()
) {

    companion object {
        private const val PREFS_NAME = "asset_prefs"
        private const val KEY_ASSETS = "assets_list"
        private const val KEY_ASSET_HISTORIES = "asset_histories"
        private const val KEY_TOTAL_ASSET_HISTORY = "total_asset_history"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 获取所有资产
     */
    fun getAllAssets(): List<DataAsset> {
        val json = sharedPreferences.getString(KEY_ASSETS, null)
        return if (json != null) {
            val type = object : TypeToken<List<DataAsset>>() {}.type
            val assets = gson.fromJson<List<DataAsset>>(json, type) ?: emptyList()
            // 修复资产ID：为ID为0的资产分配新ID，并确保ID唯一
            fixAssetIds(assets)
        } else {
            emptyList()
        }
    }

    /**
     * 修复资产ID：确保所有资产都有唯一的非零ID
     */
    private fun fixAssetIds(assets: List<DataAsset>): List<DataAsset> {
        if (assets.isEmpty()) return emptyList()

        // 找出最大ID
        val maxId = assets.maxOfOrNull { it.id } ?: 0L
        var nextId = if (maxId == 0L) 1L else maxId + 1

        val seenIds = mutableSetOf<Long>()
        val fixedAssets = mutableListOf<DataAsset>()
        var needsFix = false

        for (asset in assets) {
            var fixedAsset = asset

            // 确保所有资产都有 currency 和 type 字段（处理旧数据）
            if (fixedAsset.currency.isEmpty()) {
                fixedAsset = fixedAsset.copy(currency = "CNY")
            }
            if (fixedAsset.type.isNullOrEmpty()) {
                fixedAsset = fixedAsset.copy(type = "OTHER")
            }

            // 检查ID是否有效（非零且唯一）
            val originalId = fixedAsset.id
            if (originalId == 0L || seenIds.contains(originalId)) {
                // 需要分配新ID
                val newId = nextId++
                fixedAsset = fixedAsset.copy(id = newId)
                needsFix = true
            }

            seenIds.add(fixedAsset.id)
            fixedAssets.add(fixedAsset)
        }

        // 如果修复了任何ID，保存修复后的资产列表
        if (needsFix) {
            saveAssets(fixedAssets)
        }

        return fixedAssets
    }

    /**
     * 获取所有资产的变化流
     * 当SharedPreferences中的资产数据发生变化时自动发射新数据
     */
    fun getAllAssetsFlow(): Flow<List<DataAsset>> = callbackFlow {
        // 初始值
        trySend(getAllAssets())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ASSETS) {
                trySend(getAllAssets())
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // 当流被取消时注销监听器
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    /**
     * 保存资产列表
     */
    fun saveAssets(assets: List<DataAsset>) {
        val json = gson.toJson(assets)
        sharedPreferences.edit().putString(KEY_ASSETS, json).apply()
    }

    /**
     * 获取资产操作记录
     */
    fun getAssetHistory(assetId: Long): List<DataAssetHistory> {
        val json = sharedPreferences.getString(KEY_ASSET_HISTORIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<DataAssetHistory>>() {}.type
            val allHistory = gson.fromJson<List<DataAssetHistory>>(json, type) ?: emptyList()
            allHistory.filter { it.assetId == assetId }.sortedByDescending { it.timestamp }
        } else {
            emptyList()
        }
    }

    /**
     * 获取所有资产操作记录
     */
    fun getAllAssetHistories(): List<DataAssetHistory> {
        val json = sharedPreferences.getString(KEY_ASSET_HISTORIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<DataAssetHistory>>() {}.type
            gson.fromJson<List<DataAssetHistory>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * 添加资产操作记录
     */
    fun addAssetHistory(history: DataAssetHistory) {
        val allHistory = getAllAssetHistories()
        val newList = allHistory + history
        val json = gson.toJson(newList)
        sharedPreferences.edit().putString(KEY_ASSET_HISTORIES, json).apply()
    }

    /**
     * 获取所有资产操作记录的变化流
     */
    fun getAllAssetHistoriesFlow(): Flow<List<DataAssetHistory>> = callbackFlow {
        // 初始值
        trySend(getAllAssetHistories())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ASSET_HISTORIES) {
                trySend(getAllAssetHistories())
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    /**
     * 获取指定资产操作记录的变化流
     */
    fun getAssetHistoryFlow(assetId: Long): Flow<List<DataAssetHistory>> =
        getAllAssetHistoriesFlow().map { allHistory ->
            allHistory.filter { it.assetId == assetId }.sortedByDescending { it.timestamp }
        }

    /**
     * 获取总资产历史快照的变化流
     */
    fun getAllTotalAssetHistoryFlow(): Flow<List<DataTotalAssetSnapshot>> = callbackFlow {
        // 初始值
        trySend(getAllTotalAssetHistory())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_TOTAL_ASSET_HISTORY) {
                trySend(getAllTotalAssetHistory())
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    /**
     * 获取总资产历史快照
     */
    fun getAllTotalAssetHistory(): List<DataTotalAssetSnapshot> {
        val json = sharedPreferences.getString(KEY_TOTAL_ASSET_HISTORY, null)
        return if (json != null) {
            val type = object : TypeToken<List<DataTotalAssetSnapshot>>() {}.type
            gson.fromJson<List<DataTotalAssetSnapshot>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * 添加总资产快照
     */
    fun addTotalAssetSnapshot(snapshot: DataTotalAssetSnapshot) {
        val allHistory = getAllTotalAssetHistory()
        val newList = allHistory + snapshot
        val json = gson.toJson(newList)
        sharedPreferences.edit().putString(KEY_TOTAL_ASSET_HISTORY, json).apply()
    }

    /**
     * 删除指定资产的所有操作记录
     */
    fun deleteHistoriesByAssetId(assetId: Long) {
        val allHistory = getAllAssetHistories()
        val filteredHistory = allHistory.filter { it.assetId != assetId }
        val json = gson.toJson(filteredHistory)
        sharedPreferences.edit().putString(KEY_ASSET_HISTORIES, json).apply()
    }

    /**
     * 保留指定资产的最后一条操作记录
     */
    fun keepOnlyLastHistoryByAssetId(assetId: Long) {
        val allHistory = getAllAssetHistories()
        val assetHistories = allHistory.filter { it.assetId == assetId }

        if (assetHistories.isEmpty()) {
            return
        }

        // 找到时间戳最大的记录（最后一条）
        val maxTimestamp = assetHistories.maxOf { it.timestamp }

        // 保留时间戳最大的记录，删除该资产的其他所有记录
        val filteredHistory = allHistory.filter {
            it.assetId != assetId || it.timestamp == maxTimestamp
        }

        val json = gson.toJson(filteredHistory)
        sharedPreferences.edit().putString(KEY_ASSET_HISTORIES, json).apply()
    }

    /**
     * 清空所有数据
     */
    fun clearAllData() {
        sharedPreferences.edit().apply {
            remove(KEY_ASSETS)
            remove(KEY_ASSET_HISTORIES)
            remove(KEY_TOTAL_ASSET_HISTORY)
            apply()
        }
    }

    /**
     * 获取资产数量
     */
    fun getAssetCount(): Int {
        return getAllAssets().size
    }
}