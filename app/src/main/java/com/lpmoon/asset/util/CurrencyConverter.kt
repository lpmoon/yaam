package com.lpmoon.asset.util

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.ExchangeRate

/**
 * 货币转换工具类
 * 提供资产价值在不同货币间的转换功能
 */
object CurrencyConverter {

    /**
     * 将资产价值转换为人民币
     * @param asset 资产对象
     * @param exchangeRate 汇率信息
     * @return 人民币价值
     */
    fun convertToCny(asset: Asset, exchangeRate: ExchangeRate): Double {
        val evaluatedValue = ExpressionEvaluator.evaluate(asset.value)
        return convertToCny(evaluatedValue, asset.currency, exchangeRate)
    }

    /**
     * 将指定货币的金额转换为人民币
     * @param amount 金额
     * @param currency 货币类型（"CNY", "USD", "HKD"）
     * @param exchangeRate 汇率信息
     * @return 人民币价值
     */
    fun convertToCny(amount: Double, currency: String, exchangeRate: ExchangeRate): Double {
        val currencyType = if (currency.isBlank()) "CNY" else currency
        return when (currencyType) {
            "CNY" -> amount
            "USD" -> amount * exchangeRate.usdToCny
            "HKD" -> amount * exchangeRate.hkdToCny
            else -> amount
        }
    }
}
