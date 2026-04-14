package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.repository.AssetRepository
import kotlinx.coroutines.flow.first

/**
 * 删除资产用例
 */
class DeleteAssetUseCase(
    private val assetRepository: AssetRepository
) : UseCase<Long, Unit> {

    override suspend fun invoke(assetId: Long) {
        val currentAssets = assetRepository.getAllAssets().first()
        val updatedAssets = currentAssets.filter { it.id != assetId }

        // 保存更新后的资产列表
        assetRepository.saveAssets(updatedAssets)

        // 清理该资产的所有历史记录，避免ID重复使用时混淆
        assetRepository.deleteHistoriesByAssetId(assetId)
    }
}