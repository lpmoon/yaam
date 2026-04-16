package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase
import kotlinx.coroutines.flow.first

/**
 * 删除资产用例
 */
class DeleteAssetUseCase(
    private val assetRepository: AssetRepository,
    private val calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase
) : UseCase<Long, Unit> {

    override suspend fun invoke(assetId: Long) {
        val currentAssets = assetRepository.getAllAssets().first()
        val assetToDelete = currentAssets.find { it.id == assetId }

        if (assetToDelete != null) {
            assetRepository.deleteAsset(assetToDelete)

            // 清理该资产的所有历史记录
            assetRepository.deleteHistoriesByAssetId(assetId)

            // 创建总资产快照
            addTotalAssetSnapshot()
        }
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
