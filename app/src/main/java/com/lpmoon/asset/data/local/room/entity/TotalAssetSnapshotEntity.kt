package com.lpmoon.asset.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lpmoon.asset.domain.model.TotalAssetSnapshot

/**
 * Room 总资产快照实体
 */
@Entity(tableName = "total_asset_snapshots")
data class TotalAssetSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalValue: Double
) {
    fun toDomainModel(): TotalAssetSnapshot = TotalAssetSnapshot(
        timestamp = timestamp,
        totalValue = totalValue
    )

    companion object {
        fun fromDomainModel(snapshot: TotalAssetSnapshot): TotalAssetSnapshotEntity =
            TotalAssetSnapshotEntity(
                timestamp = snapshot.timestamp,
                totalValue = snapshot.totalValue
            )
    }
}

