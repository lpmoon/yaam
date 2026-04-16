package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.ExchangeRate
import com.lpmoon.asset.domain.usecase.UseCase


/**
 * 货币转换用例
 */
class ConvertCurrencyUseCase() : UseCase<ConvertCurrencyUseCase.Params, Double> {

    data class Params(
        val amount: Double,
        val currency: String,
        val exchangeRate: ExchangeRate
    )

    override suspend fun invoke(params: Params): Double {
        val currencyType = if (params.currency.isBlank()) "CNY" else params.currency
        return when (currencyType) {
            "CNY" -> params.amount
            "USD" -> params.amount * params.exchangeRate.usdToCny
            "HKD" -> params.amount * params.exchangeRate.hkdToCny
            else -> params.amount
        }
    }
}