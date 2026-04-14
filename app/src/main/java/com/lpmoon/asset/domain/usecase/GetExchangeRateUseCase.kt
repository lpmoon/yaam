package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.repository.ExchangeRateRepository

/**
 * 获取汇率用例
 */
class GetExchangeRateUseCase(
    private val exchangeRateRepository: ExchangeRateRepository
) : UseCaseNoParam<com.lpmoon.asset.domain.model.ExchangeRate> {

    override suspend fun invoke(): com.lpmoon.asset.domain.model.ExchangeRate {
        return exchangeRateRepository.getCachedExchangeRate()
    }
}