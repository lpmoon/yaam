package com.lpmoon.asset.data.local

import com.lpmoon.asset.data.local.room.dao.ExchangeRateDao
import com.lpmoon.asset.data.local.room.entity.ExchangeRateEntity
import com.lpmoon.asset.domain.model.asset.ExchangeRate

/**
 * 汇率本地数据源（Room 实现）
 * 负责使用 Room 数据库缓存汇率数据
 */
class ExchangeRateLocalDataSource(
    private val exchangeRateDao: ExchangeRateDao
) {

    /**
     * 获取缓存的汇率，无缓存时返回默认值
     */
    suspend fun getCachedExchangeRate(): ExchangeRate {
        return exchangeRateDao.getExchangeRate()?.toDomainModel()
            ?: ExchangeRate.getDefaultValues()
    }

    /**
     * 保存汇率
     */
    suspend fun saveExchangeRate(rate: ExchangeRate) {
        exchangeRateDao.saveExchangeRate(ExchangeRateEntity.fromDomainModel(rate))
    }

    /**
     * 清空汇率缓存
     */
    suspend fun clearCache() {
        exchangeRateDao.clearCache()
    }
}