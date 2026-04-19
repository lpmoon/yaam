package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 保存资产列表用例
 */
class SaveAssetsUseCase(
    private val assetRepository: AssetRepository,
    private val calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase
) : UseCase<List<Asset>, Unit> {

    override suspend fun invoke(params: List<Asset>) {
        assetRepository.saveAssets(params)

        // 创建总资产快照
        addTotalAssetSnapshot()
    }

    private suspend fun addTotalAssetSnapshot() {
        try {
            val totalValue = calculateTotalAssetsUseCase.calculateNow()
            val snapshot = TotalAssetSnapshot(
                timestamp = System.currentTimeMillis(),
                totalValue = totalValue
            )
            assetRepository.addTotalAssetSnapshot(snapshot)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}