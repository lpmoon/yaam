package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.model.tax.TaxSettings
import com.lpmoon.asset.domain.repository.tax.TaxSettingsRepository
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 保存税率设置用例
 */
class SaveTaxSettingsUseCase(
    private val repository: TaxSettingsRepository
) : UseCase<TaxSettings, Unit> {

    override suspend operator fun invoke(params: TaxSettings) {
        repository.saveSettings(params)
    }
}