package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.repository.asset.ExchangeRateRepository
import com.lpmoon.asset.domain.usecase.UseCaseNoParam

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