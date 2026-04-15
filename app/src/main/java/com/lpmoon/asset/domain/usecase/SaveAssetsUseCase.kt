package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.repository.AssetRepository

/**
 * 保存资产列表用例
 */
class SaveAssetsUseCase(
    private val assetRepository: AssetRepository
) : UseCase<List<com.lpmoon.asset.domain.model.Asset>, Unit> {

    override suspend fun invoke(params: List<com.lpmoon.asset.domain.model.Asset>) {
        assetRepository.saveAssets(params)
    }
}