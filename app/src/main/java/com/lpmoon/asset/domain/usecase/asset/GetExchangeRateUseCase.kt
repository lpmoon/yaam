package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.ExchangeRate
import com.lpmoon.asset.domain.repository.asset.ExchangeRateRepository
import com.lpmoon.asset.domain.usecase.UseCaseNoParam

/**
 * 获取汇率用例
 */
class GetExchangeRateUseCase(
    private val exchangeRateRepository: ExchangeRateRepository
) : UseCaseNoParam<ExchangeRate> {

    override suspend fun invoke(): ExchangeRate {
        return exchangeRateRepository.getExchangeRate()
    }
}