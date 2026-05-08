package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.model.tax.IncomeTaxHistory;
import com.lpmoon.asset.domain.repository.tax.IncomeTaxHistoryRepository;
import com.lpmoon.asset.domain.usecase.FlowUseCaseNoParam;
import kotlinx.coroutines.flow.Flow;

/**
 * 获取普通收入计算器历史记录用例
 */
class GetIncomeTaxHistoriesUseCase(
    private val repository: IncomeTaxHistoryRepository
) : FlowUseCaseNoParam<List<IncomeTaxHistory>> {

    override fun invoke(): Flow<List<IncomeTaxHistory>> {
        return repository.getHistories()
    }
}
