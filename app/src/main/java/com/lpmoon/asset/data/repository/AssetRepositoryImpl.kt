package com.lpmoon.asset.data.repository

import com.lpmoon.asset.data.local.AssetLocalDataSource
import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 资产仓库实现
 */
class AssetRepositoryImpl(
    private val localDataSource: AssetLocalDataSource
) : AssetRepository {

    override fun getAllAssets(): Flow<List<Asset>> =
        localDataSource.getAllAssetsFlow()

    override suspend fun saveAssets(assets: List<Asset>) {
        localDataSource.saveAssets(assets)
    }

    override fun getAssetHistory(assetId: Long): Flow<List<AssetHistory>> =
        localDataSource.getAssetHistoryFlow(assetId)

    override suspend fun addAssetHistory(history: AssetHistory) {
        localDataSource.addAssetHistory(history)
    }

    override fun getAllAssetHistories(): Flow<List<AssetHistory>> =
        localDataSource.getAllAssetHistoriesFlow()

    override fun getAllTotalAssetHistory(): Flow<List<TotalAssetSnapshot>> =
        localDataSource.getAllTotalAssetHistoryFlow()

    override suspend fun addTotalAssetSnapshot(snapshot: TotalAssetSnapshot) {
        localDataSource.addTotalAssetSnapshot(snapshot)
    }

    override suspend fun deleteHistoriesByAssetId(assetId: Long) {
        localDataSource.deleteHistoriesByAssetId(assetId)
    }

    override suspend fun keepOnlyLastHistoryByAssetId(assetId: Long) {
        localDataSource.keepOnlyLastHistoryByAssetId(assetId)
    }

    override suspend fun clearAllData() {
        localDataSource.clearAllData()
    }

    override fun getAssetCount(): Flow<Int> =
        getAllAssets().map { it.size }
}
