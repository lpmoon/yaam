package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.FlowUseCase
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