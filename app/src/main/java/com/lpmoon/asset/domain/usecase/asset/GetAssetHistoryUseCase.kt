package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 获取资产历史用例
 */
class GetAssetHistoryUseCase(
    private val assetRepository: AssetRepository
) : UseCase<Long, List<AssetHistory>> {

    override suspend operator fun invoke(assetId: Long): List<AssetHistory> {
        return assetRepository.getAssetHistory(assetId)
    }
}
