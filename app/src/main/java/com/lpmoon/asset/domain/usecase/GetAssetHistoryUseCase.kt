package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

/**
 * 获取资产历史用例
 */
class GetAssetHistoryUseCase(
    private val assetRepository: AssetRepository
) : FlowUseCase<Long, List<AssetHistory>> {

    override fun invoke(assetId: Long): Flow<List<AssetHistory>> {
        return assetRepository.getAssetHistory(assetId)
    }
}