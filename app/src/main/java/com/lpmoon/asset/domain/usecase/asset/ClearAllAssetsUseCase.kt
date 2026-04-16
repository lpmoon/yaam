package com.lpmoon.asset.domain.usecase.asset

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
    }
}