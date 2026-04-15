package com.lpmoon.asset.domain.repository

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.TotalAssetSnapshot
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
     * 保存资产列表
     */
    suspend fun saveAssets(assets: List<Asset>)

    /**
     * 获取指定资产的操作记录
     */
    fun getAssetHistory(assetId: Long): Flow<List<AssetHistory>>

    /**
     * 添加资产操作记录
     */
    suspend fun addAssetHistory(history: AssetHistory)

    /**
     * 获取所有资产操作记录
     */
    fun getAllAssetHistories(): Flow<List<AssetHistory>>

    /**
     * 获取总资产历史快照
     */
    fun getAllTotalAssetHistory(): Flow<List<TotalAssetSnapshot>>

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