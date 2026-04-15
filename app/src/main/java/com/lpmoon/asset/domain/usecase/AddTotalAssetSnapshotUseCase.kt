package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.AssetRepository

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