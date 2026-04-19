package com.lpmoon.asset.domain.repository.asset

import com.lpmoon.asset.domain.model.asset.ExchangeRate

/**
 * 汇率数据仓库接口
 * 定义汇率相关的数据操作契约
 */
interface ExchangeRateRepository {

    /**
     * 获取当前汇率
     * 如果缓存过期则自动更新
     */
    suspend fun getExchangeRate(): ExchangeRate

    /**
     * 强制更新汇率
     */
    suspend fun updateExchangeRate(): ExchangeRate

    /**
     * 获取缓存的汇率（不触发更新）
     */
    suspend fun getCachedExchangeRate(): ExchangeRate
}