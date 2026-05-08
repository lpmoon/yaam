package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.model.tax.IncomeTaxHistory;
import com.lpmoon.asset.domain.repository.tax.IncomeTaxHistoryRepository;
import com.lpmoon.asset.domain.usecase.UseCase;

/**
 * 保存普通收入计算器历史记录用例
 */
class SaveIncomeTaxHistoryUseCase(
    private val repository: IncomeTaxHistoryRepository
) : UseCase<IncomeTaxHistory, Unit> {

    override suspend operator fun invoke(params: IncomeTaxHistory) {
        repository.saveHistory(params)
    }
}
