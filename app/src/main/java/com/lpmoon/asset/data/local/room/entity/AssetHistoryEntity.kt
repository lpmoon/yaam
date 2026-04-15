package com.lpmoon.asset.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.OperationType

/**
 * Room 资产操作记录实体
 */
@Entity(tableName = "asset_histories")
data class AssetHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val oldValue: String,
    val newValue: String,
    val timestamp: Long = System.currentTimeMillis(),
    val operationType: String = OperationType.UPDATE.name
) {
    fun toDomainModel(): AssetHistory = AssetHistory(
        id = id,
        assetId = assetId,
        oldValue = oldValue,
        newValue = newValue,
        timestamp = timestamp,
        operationType = OperationType.valueOf(operationType)
    )

    companion object {
        fun fromDomainModel(history: AssetHistory): AssetHistoryEntity = AssetHistoryEntity(
            id = history.id,
            assetId = history.assetId,
            oldValue = history.oldValue,
            newValue = history.newValue,
            timestamp = history.timestamp,
            operationType = history.operationType.name
        )
    }
}

