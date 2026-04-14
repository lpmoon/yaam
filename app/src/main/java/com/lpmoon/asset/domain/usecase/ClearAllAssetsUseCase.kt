package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.repository.AssetRepository

/**
 * 清空所有资产数据用例
 */
class ClearAllAssetsUseCase(
    private val assetRepository: AssetRepository
) : UseCaseNoParam<Unit> {

    override suspend fun invoke() {
        assetRepository.clearAllData()
    }
}