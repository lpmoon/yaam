package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase
import kotlinx.coroutines.flow.first

/**
 * 删除资产用例
 */
class DeleteAssetUseCase(
    private val assetRepository: AssetRepository
) : UseCase<Long, Unit> {

    override suspend fun invoke(assetId: Long) {
        val currentAssets = assetRepository.getAllAssets().first()
        // 只删除第一个匹配的资产（防止ID重复时删除多个资产）
        var deleted = false
        val updatedAssets = currentAssets.filter { asset ->
            if (!deleted && asset.id == assetId) {
                deleted = true
                false // 不包含这个资产
            } else {
                true // 包含其他资产
            }
        }

        // 保存更新后的资产列表
        assetRepository.saveAssets(updatedAssets)

        // 清理该资产的所有历史记录，避免ID重复使用时混淆
        assetRepository.deleteHistoriesByAssetId(assetId)
    }
}