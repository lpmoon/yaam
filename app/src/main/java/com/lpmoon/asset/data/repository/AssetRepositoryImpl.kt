package com.lpmoon.asset.data.repository

import com.lpmoon.asset.data.local.AssetLocalDataSource
import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.asset.AssetRepository
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

    override suspend fun addAsset(asset: Asset): Long {
        return localDataSource.addAsset(asset)
    }

    override suspend fun updateAsset(asset: Asset) {
        localDataSource.updateAsset(asset)
    }

    override suspend fun deleteAsset(asset: Asset) {
        localDataSource.deleteAsset(asset)
    }

    override suspend fun saveAssets(assets: List<Asset>) {
        localDataSource.saveAssets(assets)
    }

    override suspend fun getAssetHistory(assetId: Long): List<AssetHistory> =
        localDataSource.getAssetHistory(assetId)

    override suspend fun addAssetHistory(history: AssetHistory) {
        localDataSource.addAssetHistory(history)
    }

    override suspend fun getAllAssetHistories(): List<AssetHistory> =
        localDataSource.getAllAssetHistories()

    override suspend fun getAllTotalAssetHistory(): List<TotalAssetSnapshot> =
        localDataSource.getAllTotalAssetHistory()

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
