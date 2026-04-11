package com.lpmoon.asset.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lpmoon.asset.data.AssetType
import com.lpmoon.asset.data.CurrencyType

class AssetRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("asset_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_ASSETS = "assets_list"
        private const val KEY_ASSET_HISTORIES = "asset_histories"
        private const val KEY_TOTAL_ASSET_HISTORY = "total_asset_history"
    }

    fun getAllAssets(): List<Asset> {
        val json = sharedPreferences.getString(KEY_ASSETS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Asset>>() {}.type
            val assets = gson.fromJson<List<Asset>>(json, type) ?: emptyList()
            // 确保所有资产都有 currency 和 type 字段（处理旧数据）
            assets.map { asset ->
                var updatedAsset = asset
                if (asset.currency.isEmpty()) {
                    updatedAsset = updatedAsset.copy(currency = CurrencyType.CNY.name)
                }
                if (asset.type.isNullOrEmpty()) {
                    updatedAsset = updatedAsset.copy(type = AssetType.OTHER.name)
                }
                updatedAsset
            }
        } else {
            emptyList()
        }
    }

    fun saveAssets(assets: List<Asset>) {
        val json = gson.toJson(assets)
        sharedPreferences.edit().putString(KEY_ASSETS, json).apply()
    }

    fun getAssetHistory(assetId: Long): List<AssetHistory> {
        val json = sharedPreferences.getString(KEY_ASSET_HISTORIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<AssetHistory>>() {}.type
            val allHistory = gson.fromJson<List<AssetHistory>>(json, type) ?: emptyList()
            allHistory.filter { it.assetId == assetId }.sortedByDescending { it.timestamp }
        } else {
            emptyList()
        }
    }

    fun addAssetHistory(history: AssetHistory) {
        val allHistory = getAllAssetHistories()
        val newList = allHistory + history
        val json = gson.toJson(newList)
        sharedPreferences.edit().putString(KEY_ASSET_HISTORIES, json).apply()
    }

    fun getAllAssetHistories(): List<AssetHistory> {
        val json = sharedPreferences.getString(KEY_ASSET_HISTORIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<AssetHistory>>() {}.type
            gson.fromJson<List<AssetHistory>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getAllTotalAssetHistory(): List<TotalAssetSnapshot> {
        val json = sharedPreferences.getString(KEY_TOTAL_ASSET_HISTORY, null)
        return if (json != null) {
            val type = object : TypeToken<List<TotalAssetSnapshot>>() {}.type
            gson.fromJson<List<TotalAssetSnapshot>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun addTotalAssetSnapshot(snapshot: TotalAssetSnapshot) {
        val allHistory = getAllTotalAssetHistory()
        val newList = allHistory + snapshot
        val json = gson.toJson(newList)
        sharedPreferences.edit().putString(KEY_TOTAL_ASSET_HISTORY, json).apply()
    }

    fun clearTotalAssetHistory() {
        sharedPreferences.edit().remove(KEY_TOTAL_ASSET_HISTORY).apply()
    }

    fun clearAllData() {
        sharedPreferences.edit().apply {
            remove(KEY_ASSETS)
            remove(KEY_ASSET_HISTORIES)
            remove(KEY_TOTAL_ASSET_HISTORY)
            apply()
        }
    }
}