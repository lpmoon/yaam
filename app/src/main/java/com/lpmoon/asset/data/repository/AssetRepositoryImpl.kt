package com.lpmoon.asset.data.repository

import com.lpmoon.asset.data.local.AssetLocalDataSource
import com.lpmoon.asset.data.mapper.AssetMapper
import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * 资产仓库实现
 */
class AssetRepositoryImpl(
    private val localDataSource: AssetLocalDataSource,
    private val mapper: AssetMapper
) : AssetRepository {

    override fun getAllAssets(): Flow<List<Asset>> =
        localDataSource.getAllAssetsFlow().map { dataAssets ->
            mapper.mapAssetsToDomain(dataAssets)
        }

    override suspend fun saveAssets(assets: List<Asset>) {
        val dataAssets = mapper.mapAssetsToData(assets)
        localDataSource.saveAssets(dataAssets)
    }

    override fun getAssetHistory(assetId: Long): Flow<List<AssetHistory>> =
        localDataSource.getAssetHistoryFlow(assetId).map { dataHistories ->
            mapper.mapHistoriesToDomain(dataHistories)
        }

    override suspend fun addAssetHistory(history: AssetHistory) {
        val dataHistory = mapper.mapToData(history)
        localDataSource.addAssetHistory(dataHistory)
    }

    override fun getAllAssetHistories(): Flow<List<AssetHistory>> =
        localDataSource.getAllAssetHistoriesFlow().map { dataHistories ->
            mapper.mapHistoriesToDomain(dataHistories)
        }

    override fun getAllTotalAssetHistory(): Flow<List<TotalAssetSnapshot>> =
        localDataSource.getAllTotalAssetHistoryFlow().map { dataSnapshots ->
            mapper.mapSnapshotsToDomain(dataSnapshots)
        }

    override suspend fun addTotalAssetSnapshot(snapshot: TotalAssetSnapshot) {
        val dataSnapshot = mapper.mapToData(snapshot)
        localDataSource.addTotalAssetSnapshot(dataSnapshot)
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