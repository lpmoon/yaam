package com.lpmoon.asset.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lpmoon.asset.domain.model.Asset

/**
 * Room 资产实体
 */
@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val value: String,
    val currency: String = "CNY",
    val type: String = "OTHER"
) {
    fun toDomainModel(): Asset = Asset(
        id = id,
        name = name,
        value = value,
        currency = currency,
        type = type
    )

    companion object {
        fun fromDomainModel(asset: Asset): AssetEntity = AssetEntity(
            id = asset.id,
            name = asset.name,
            value = asset.value,
            currency = asset.currency,
            type = asset.type
        )
    }
}

