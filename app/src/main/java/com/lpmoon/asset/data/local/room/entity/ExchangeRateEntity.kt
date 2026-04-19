package com.lpmoon.asset.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lpmoon.asset.domain.model.asset.ExchangeRate

/**
 * Room 汇率缓存实体（只保存单条最新记录，id 固定为 1）
 */
@Entity(tableName = "exchange_rate")
data class ExchangeRateEntity(
    @PrimaryKey
    val id: Int = 1,
    val usdToCny: Double,
    val hkdToCny: Double,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): ExchangeRate = ExchangeRate(
        usdToCny = usdToCny,
        hkdToCny = hkdToCny,
        lastUpdateTime = lastUpdateTime
    )

    companion object {
        fun fromDomainModel(rate: ExchangeRate): ExchangeRateEntity = ExchangeRateEntity(
            usdToCny = rate.usdToCny,
            hkdToCny = rate.hkdToCny,
            lastUpdateTime = rate.lastUpdateTime
        )
    }
}

