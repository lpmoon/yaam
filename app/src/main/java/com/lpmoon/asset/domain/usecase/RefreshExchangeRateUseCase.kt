package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.repository.ExchangeRateRepository

/**
 * 刷新汇率用例
 */
class RefreshExchangeRateUseCase(
    private val exchangeRateRepository: ExchangeRateRepository
) : UseCaseNoParam<Unit> {

    override suspend fun invoke() {
        exchangeRateRepository.updateExchangeRate()
    }
}