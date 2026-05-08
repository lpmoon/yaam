package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.repository.tax.IncomeTaxHistoryRepository;
import com.lpmoon.asset.domain.usecase.UseCaseNoParam;

/**
 * 清除所有普通收入计算器历史记录用例
 */
class ClearIncomeTaxHistoriesUseCase(
    private val repository: IncomeTaxHistoryRepository
) : UseCaseNoParam<Unit> {

    override suspend operator fun invoke() {
        repository.clearAllHistories()
    }
}
