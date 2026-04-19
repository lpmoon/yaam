package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.model.tax.TaxSettings
import com.lpmoon.asset.domain.repository.tax.TaxSettingsRepository
import com.lpmoon.asset.domain.usecase.UseCaseNoParam

/**
 * 加载税率设置用例
 */
class LoadTaxSettingsUseCase(
    private val repository: TaxSettingsRepository
) : UseCaseNoParam<TaxSettings> {

    override suspend operator fun invoke(): TaxSettings {
        return repository.loadSettings()
    }
}