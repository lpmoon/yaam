package com.lpmoon.asset.data.local

import com.lpmoon.asset.data.local.room.dao.AssetDao
import com.lpmoon.asset.data.local.room.dao.AssetHistoryDao
import com.lpmoon.asset.data.local.room.dao.TotalAssetSnapshotDao
import com.lpmoon.asset.data.local.room.entity.AssetEntity
import com.lpmoon.asset.data.local.room.entity.AssetHistoryEntity
import com.lpmoon.asset.data.local.room.entity.TotalAssetSnapshotEntity
import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.TotalAssetSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 资产本地数据源（Room 实现）
 * 负责使用 Room 数据库存储和检索资产数据
 */
class AssetLocalDataSource(
    private val assetDao: AssetDao,
    private val assetHistoryDao: AssetHistoryDao,
    private val totalAssetSnapshotDao: TotalAssetSnapshotDao
) {

    /**
     * 获取所有资产（一次性）
     */
    suspend fun getAllAssets(): List<Asset> =
        assetDao.getAllAssets().map { it.toDomainModel() }

    /**
     * 获取所有资产的变化流
     */
    fun getAllAssetsFlow(): Flow<List<Asset>> =
        assetDao.getAllAssetsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }

    /**
     * 保存资产列表（全量替换）
     */
    suspend fun saveAssets(assets: List<Asset>) {
        assetDao.deleteAllAssets()
        assetDao.insertAssets(assets.map { AssetEntity.fromDomainModel(it) })
    }

    /**
     * 获取资产操作记录（一次性）
     */
    suspend fun getAssetHistory(assetId: Long): List<AssetHistory> =
        emptyList() // 通过 flow 使用

    /**
     * 获取指定资产操作记录的变化流
     */
    fun getAssetHistoryFlow(assetId: Long): Flow<List<AssetHistory>> =
        assetHistoryDao.getHistoriesByAssetIdFlow(assetId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    /**
     * 获取所有资产操作记录（一次性）
     */
    suspend fun getAllAssetHistories(): List<AssetHistory> = emptyList() // 通过 flow 使用

    /**
     * 获取所有资产操作记录的变化流
     */
    fun getAllAssetHistoriesFlow(): Flow<List<AssetHistory>> =
        assetHistoryDao.getAllHistoriesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }

    /**
     * 添加资产操作记录
     */
    suspend fun addAssetHistory(history: AssetHistory) {
        assetHistoryDao.insertHistory(AssetHistoryEntity.fromDomainModel(history))
    }

    /**
     * 获取总资产历史快照的变化流
     */
    fun getAllTotalAssetHistoryFlow(): Flow<List<TotalAssetSnapshot>> =
        totalAssetSnapshotDao.getAllSnapshotsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }

    /**
     * 添加总资产快照
     */
    suspend fun addTotalAssetSnapshot(snapshot: TotalAssetSnapshot) {
        totalAssetSnapshotDao.insertSnapshot(TotalAssetSnapshotEntity.fromDomainModel(snapshot))
    }

    /**
     * 删除指定资产的所有操作记录
     */
    suspend fun deleteHistoriesByAssetId(assetId: Long) {
        assetHistoryDao.deleteHistoriesByAssetId(assetId)
    }

    /**
     * 保留指定资产的最后一条操作记录
     */
    suspend fun keepOnlyLastHistoryByAssetId(assetId: Long) {
        assetHistoryDao.keepOnlyLastHistoryByAssetId(assetId)
    }

    /**
     * 清空所有数据
     */
    suspend fun clearAllData() {
        assetDao.deleteAllAssets()
        assetHistoryDao.deleteAllHistories()
        totalAssetSnapshotDao.deleteAllSnapshots()
    }

    /**
     * 获取资产数量流
     */
    fun getAssetCountFlow(): Flow<Int> =
        assetDao.getAssetCountFlow()
}
