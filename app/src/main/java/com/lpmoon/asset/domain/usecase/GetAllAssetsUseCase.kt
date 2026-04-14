package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

/**
 * 获取所有资产用例
 */
class GetAllAssetsUseCase(
    private val assetRepository: AssetRepository
) : FlowUseCaseNoParam<List<com.lpmoon.asset.domain.model.Asset>> {

    override fun invoke(): Flow<List<com.lpmoon.asset.domain.model.Asset>> {
        return assetRepository.getAllAssets()
    }
}