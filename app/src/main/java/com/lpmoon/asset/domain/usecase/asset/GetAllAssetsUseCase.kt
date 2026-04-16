package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.FlowUseCaseNoParam
import kotlinx.coroutines.flow.Flow

/**
 * 获取所有资产用例
 */
class GetAllAssetsUseCase(
    private val assetRepository: AssetRepository
) : FlowUseCaseNoParam<List<Asset>> {

    override fun invoke(): Flow<List<Asset>> {
        return assetRepository.getAllAssets()
    }
}