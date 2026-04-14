package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.ExchangeRate
import com.lpmoon.asset.domain.repository.AssetRepository
import com.lpmoon.asset.domain.repository.ExchangeRateRepository
import com.lpmoon.asset.util.ExpressionEvaluator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * 计算总资产用例
 */
class CalculateTotalAssetsUseCase(
    private val assetRepository: AssetRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : FlowUseCaseNoParam<Double> {

    override fun invoke(): Flow<Double> {
        // 创建一个Flow来监听资产和汇率的变化
        return combine(
            assetRepository.getAllAssets(),
            createExchangeRateFlow()
        ) { assets, exchangeRate ->
            calculateTotalAssets(assets, exchangeRate)
        }
    }

    private fun calculateTotalAssets(assets: List<Asset>, exchangeRate: ExchangeRate): Double {
        return assets.sumOf { asset ->
            // 1. 计算表达式值
            val evaluatedValue = ExpressionEvaluator.evaluate(asset.value)
            // 2. 转换为人民币
            convertCurrency(evaluatedValue, asset.currency, exchangeRate)
        }
    }

    private fun createExchangeRateFlow(): Flow<ExchangeRate> {
        return flow {
            var lastExchangeRate: ExchangeRate? = null
            while (true) {
                try {
                    val exchangeRate = exchangeRateRepository.getCachedExchangeRate()
                    if (exchangeRate != lastExchangeRate) {
                        emit(exchangeRate)
                        lastExchangeRate = exchangeRate
                    }
                } catch (e: Exception) {
                    // 如果获取汇率失败，使用默认值
                    emit(ExchangeRate.getDefaultValues())
                }
                // 等待一段时间后再次检查
                delay(30000) // 每30秒检查一次
            }
        }.distinctUntilChanged()
    }

    private fun convertCurrency(amount: Double, currency: String, exchangeRate: ExchangeRate): Double {
        val currencyType = if (currency.isBlank()) "CNY" else currency
        return when (currencyType) {
            "CNY" -> amount
            "USD" -> amount * exchangeRate.usdToCny
            "HKD" -> amount * exchangeRate.hkdToCny
            else -> amount
        }
    }
}