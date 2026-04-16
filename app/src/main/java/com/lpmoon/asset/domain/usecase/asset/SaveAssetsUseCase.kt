package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 保存资产列表用例
 */
class SaveAssetsUseCase(
    private val assetRepository: AssetRepository
) : UseCase<List<Asset>, Unit> {

    override suspend fun invoke(params: List<Asset>) {
        assetRepository.saveAssets(params)
    }
}