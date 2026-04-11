package com.lpmoon.asset.data

data class ExchangeRate(
    val usdToCny: Double,
    val hkdToCny: Double,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    companion object {
        // 默认汇率（网络请求失败时使用）
        fun getDefaultValues() = ExchangeRate(
            usdToCny = 7.2,  // 1美元 = 7.2人民币
            hkdToCny = 0.92  // 1港币 = 0.92人民币
        )
    }

    fun isExpired(): Boolean {
        // 1小时 = 3600000毫秒
        val oneHour = 3600000L
        return System.currentTimeMillis() - lastUpdateTime > oneHour
    }
}
