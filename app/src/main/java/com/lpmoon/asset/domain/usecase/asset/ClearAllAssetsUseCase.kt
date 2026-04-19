package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCaseNoParam

/**
 * 清空所有资产数据用例
 */
class ClearAllAssetsUseCase(
    private val assetRepository: AssetRepository
) : UseCaseNoParam<Unit> {

    override suspend fun invoke() {
        assetRepository.clearAllData()

        // 创建总资产为0的快照
        addTotalAssetSnapshot()
    }

    private suspend fun addTotalAssetSnapshot() {
        try {
            val snapshot = TotalAssetSnapshot(
                timestamp = System.currentTimeMillis(),
                totalValue = 0.0
            )
            assetRepository.addTotalAssetSnapshot(snapshot)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}