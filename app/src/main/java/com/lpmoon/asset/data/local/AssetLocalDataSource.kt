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
            // 确保所有资产都有 currency 和 type 字段（处理旧数据）
            assets.map { asset ->
                var updatedAsset = asset
                if (asset.currency.isEmpty()) {
                    updatedAsset = updatedAsset.copy(currency = "CNY")
                }
                if (asset.type.isNullOrEmpty()) {
                    updatedAsset = updatedAsset.copy(type = "OTHER")
                }
                updatedAsset
            }
        } else {
            emptyList()
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