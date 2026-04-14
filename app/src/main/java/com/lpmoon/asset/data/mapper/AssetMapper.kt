package com.lpmoon.asset.data.mapper

import com.lpmoon.asset.data.asset.Asset as DataAsset
import com.lpmoon.asset.data.asset.AssetHistory as DataAssetHistory
import com.lpmoon.asset.data.asset.TotalAssetSnapshot as DataTotalAssetSnapshot
import com.lpmoon.asset.domain.model.Asset as DomainAsset
import com.lpmoon.asset.domain.model.AssetHistory as DomainAssetHistory
import com.lpmoon.asset.domain.model.TotalAssetSnapshot as DomainTotalAssetSnapshot

/**
 * 资产数据映射器
 * 负责数据层模型和领域层模型之间的转换
 */
class AssetMapper {

    /**
     * 数据层资产 → 领域层资产
     */
    fun mapToDomain(asset: DataAsset): DomainAsset {
        return DomainAsset(
            id = asset.id,
            name = asset.name,
            value = asset.value,
            currency = asset.currency,
            type = asset.type
        )
    }

    /**
     * 领域层资产 → 数据层资产
     */
    fun mapToData(asset: DomainAsset): DataAsset {
        return DataAsset(
            id = asset.id,
            name = asset.name,
            value = asset.value,
            currency = asset.currency,
            type = asset.type
        )
    }

    /**
     * 数据层资产历史 → 领域层资产历史
     */
    fun mapToDomain(history: DataAssetHistory): DomainAssetHistory {
        return DomainAssetHistory(
            id = history.id,
            assetId = history.assetId,
            oldValue = history.oldValue,
            newValue = history.newValue,
            timestamp = history.timestamp,
            operationType = history.operationType
        )
    }

    /**
     * 领域层资产历史 → 数据层资产历史
     */
    fun mapToData(history: DomainAssetHistory): DataAssetHistory {
        return DataAssetHistory(
            id = history.id,
            assetId = history.assetId,
            oldValue = history.oldValue,
            newValue = history.newValue,
            timestamp = history.timestamp,
            operationType = history.operationType
        )
    }

    /**
     * 数据层总资产快照 → 领域层总资产快照
     */
    fun mapToDomain(snapshot: DataTotalAssetSnapshot): DomainTotalAssetSnapshot {
        return DomainTotalAssetSnapshot(
            timestamp = snapshot.timestamp,
            totalValue = snapshot.totalValue
        )
    }

    /**
     * 领域层总资产快照 → 数据层总资产快照
     */
    fun mapToData(snapshot: DomainTotalAssetSnapshot): DataTotalAssetSnapshot {
        return DataTotalAssetSnapshot(
            timestamp = snapshot.timestamp,
            totalValue = snapshot.totalValue
        )
    }

    /**
     * 批量转换：数据层资产列表 → 领域层资产列表
     */
    fun mapAssetsToDomain(assets: List<DataAsset>): List<DomainAsset> {
        return assets.map { mapToDomain(it) }
    }

    /**
     * 批量转换：领域层资产列表 → 数据层资产列表
     */
    fun mapAssetsToData(assets: List<DomainAsset>): List<DataAsset> {
        return assets.map { mapToData(it) }
    }

    /**
     * 批量转换：数据层资产历史列表 → 领域层资产历史列表
     */
    fun mapHistoriesToDomain(histories: List<DataAssetHistory>): List<DomainAssetHistory> {
        return histories.map { mapToDomain(it) }
    }

    /**
     * 批量转换：领域层资产历史列表 → 数据层资产历史列表
     */
    fun mapHistoriesToData(histories: List<DomainAssetHistory>): List<DataAssetHistory> {
        return histories.map { mapToData(it) }
    }

    /**
     * 批量转换：数据层总资产快照列表 → 领域层总资产快照列表
     */
    fun mapSnapshotsToDomain(snapshots: List<DataTotalAssetSnapshot>): List<DomainTotalAssetSnapshot> {
        return snapshots.map { mapToDomain(it) }
    }

    /**
     * 批量转换：领域层总资产快照列表 → 数据层总资产快照列表
     */
    fun mapSnapshotsToData(snapshots: List<DomainTotalAssetSnapshot>): List<DataTotalAssetSnapshot> {
        return snapshots.map { mapToData(it) }
    }
}