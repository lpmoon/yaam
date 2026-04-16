package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.model.tax.TaxSettings
import com.lpmoon.asset.domain.repository.tax.TaxSettingsRepository
import com.lpmoon.asset.domain.usecase.FlowUseCaseNoParam
import kotlinx.coroutines.flow.Flow

/**
 * 观察税率设置用例
 * 监听税率设置的变化
 */
class ObserveTaxSettingsUseCase(
    private val repository: TaxSettingsRepository
) : FlowUseCaseNoParam<TaxSettings> {

    override fun invoke(): Flow<TaxSettings> {
        return repository.getTaxSettings()
    }
}
