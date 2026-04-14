package com.lpmoon.asset.data.local

import android.content.Context
import android.content.SharedPreferences
import com.lpmoon.asset.domain.model.ExchangeRate

/**
 * 汇率本地数据源
 * 负责使用SharedPreferences缓存汇率数据
 */
class ExchangeRateLocalDataSource(
    private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "exchange_rate_prefs"
        private const val KEY_USD_TO_CNY = "usd_to_cny"
        private const val KEY_HKD_TO_CNY = "hkd_to_cny"
        private const val KEY_LAST_UPDATE = "last_update_time"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 获取缓存的汇率
     */
    fun getCachedExchangeRate(): ExchangeRate {
        val usdToCny = sharedPreferences.getFloat(KEY_USD_TO_CNY, 7.2f).toDouble()
        val hkdToCny = sharedPreferences.getFloat(KEY_HKD_TO_CNY, 0.92f).toDouble()
        val lastUpdateTime = sharedPreferences.getLong(KEY_LAST_UPDATE, 0)

        return ExchangeRate(
            usdToCny = usdToCny,
            hkdToCny = hkdToCny,
            lastUpdateTime = lastUpdateTime
        )
    }

    /**
     * 保存汇率
     */
    fun saveExchangeRate(rate: ExchangeRate) {
        sharedPreferences.edit().apply {
            putFloat(KEY_USD_TO_CNY, rate.usdToCny.toFloat())
            putFloat(KEY_HKD_TO_CNY, rate.hkdToCny.toFloat())
            putLong(KEY_LAST_UPDATE, rate.lastUpdateTime)
            apply()
        }
    }

    /**
     * 清空汇率缓存
     */
    fun clearCache() {
        sharedPreferences.edit().clear().apply()
    }
}