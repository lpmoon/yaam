package com.lpmoon.asset.domain.repository.asset

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * 资产数据仓库接口
 * 定义资产相关的数据操作契约，不关心具体实现
 */
interface AssetRepository {

    /**
     * 获取所有资产
     */
    fun getAllAssets(): Flow<List<Asset>>

    /**
     * 添加单个资产，返回自动生成的 ID
     */
    suspend fun addAsset(asset: Asset): Long

    /**
     * 更新单个资产
     */
    suspend fun updateAsset(asset: Asset)

    /**
     * 删除单个资产
     */
    suspend fun deleteAsset(asset: Asset)

    /**
     * 保存资产列表
     */
    suspend fun saveAssets(assets: List<Asset>)

    /**
     * 获取指定资产的操作记录
     */
    suspend fun getAssetHistory(assetId: Long): List<AssetHistory>

    /**
     * 添加资产操作记录
     */
    suspend fun addAssetHistory(history: AssetHistory)

    /**
     * 获取所有资产操作记录
     */
    suspend fun getAllAssetHistories(): List<AssetHistory>

    /**
     * 获取总资产历史快照
     */
    suspend fun getAllTotalAssetHistory(): List<TotalAssetSnapshot>

    /**
     * 添加总资产快照
     */
    suspend fun addTotalAssetSnapshot(snapshot: TotalAssetSnapshot)

    /**
     * 删除指定资产的所有操作记录
     */
    suspend fun deleteHistoriesByAssetId(assetId: Long)

    /**
     * 保留指定资产的最后一条操作记录
     */
    suspend fun keepOnlyLastHistoryByAssetId(assetId: Long)

    /**
     * 清空所有数据
     */
    suspend fun clearAllData()

    /**
     * 获取资产数量
     */
    fun getAssetCount(): Flow<Int>
}