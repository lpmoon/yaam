package com.lpmoon.asset.data.repository

import com.lpmoon.asset.data.local.ExchangeRateLocalDataSource
import com.lpmoon.asset.data.remote.ExchangeRateApiDataSource
import com.lpmoon.asset.domain.model.asset.ExchangeRate
import com.lpmoon.asset.domain.repository.asset.ExchangeRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 汇率仓库实现
 */
class ExchangeRateRepositoryImpl(
    private val localDataSource: ExchangeRateLocalDataSource,
    private val apiDataSource: ExchangeRateApiDataSource
) : ExchangeRateRepository {

    override suspend fun getExchangeRate(): ExchangeRate = withContext(Dispatchers.IO) {
        val cached = localDataSource.getCachedExchangeRate()

        // 如果缓存未过期，直接返回
        if (!cached.isExpired()) {
            return@withContext cached
        }

        // 否则尝试更新
        return@withContext try {
            updateExchangeRate()
        } catch (e: Exception) {
            // 更新失败，返回缓存的汇率
            cached
        }
    }

    override suspend fun updateExchangeRate(): ExchangeRate = withContext(Dispatchers.IO) {
        try {
            // 尝试从 API 获取汇率
            val apiRates = apiDataSource.fetchExchangeRates()

            val (usdToCny, hkdToCny) = if (apiRates != null) {
                apiRates
            } else {
                // API获取失败，使用默认汇率
                Pair(7.2, 0.92)
            }

            val newRate = ExchangeRate(
                usdToCny = usdToCny,
                hkdToCny = hkdToCny,
                lastUpdateTime = System.currentTimeMillis()
            )

            localDataSource.saveExchangeRate(newRate)
            newRate
        } catch (e: Exception) {
            // 获取汇率失败，返回默认值
            ExchangeRate.getDefaultValues()
        }
    }

    override suspend fun getCachedExchangeRate(): ExchangeRate =
        withContext(Dispatchers.IO) {
            localDataSource.getCachedExchangeRate()
        }
}