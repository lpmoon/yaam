package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 添加总资产快照用例
 */
class AddTotalAssetSnapshotUseCase(
    private val assetRepository: AssetRepository
) : UseCase<Double, Unit> {

    override suspend fun invoke(totalValue: Double) {
        val snapshot = TotalAssetSnapshot(
            timestamp = System.currentTimeMillis(),
            totalValue = totalValue
        )
        assetRepository.addTotalAssetSnapshot(snapshot)
    }
}