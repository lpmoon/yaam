package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.repository.tax.IncomeTaxHistoryRepository;
import com.lpmoon.asset.domain.usecase.UseCase;

/**
 * 删除普通收入计算器历史记录用例
 */
class DeleteIncomeTaxHistoryUseCase(
    private val repository: IncomeTaxHistoryRepository
) : UseCase<Long, Unit> {

    override suspend operator fun invoke(params: Long) {
        repository.deleteHistory(params)
    }
}
