package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.OperationType
import com.lpmoon.asset.domain.repository.AssetRepository
import kotlinx.coroutines.flow.first

/**
 * 更新资产用例
 */
class UpdateAssetUseCase(
    private val assetRepository: AssetRepository
) : UseCase<UpdateAssetUseCase.Params, Unit> {

    data class Params(
        val assetId: Long,
        val name: String,
        val value: String,
        val currency: String,
        val type: String
    )

    override suspend fun invoke(params: Params) {
        val currentAssets = assetRepository.getAllAssets().first()
        val oldAsset = currentAssets.find { it.id == params.assetId }

        if (oldAsset == null) {
            // 资产不存在，可能是竞态��件，记录错误但不抛出异常
            return

        }

        val updatedAssets = currentAssets.map { asset ->
            if (asset.id == params.assetId) {
                asset.copy(
                    name = params.name,
                    value = params.value,
                    currency = params.currency,
                    type = params.type

                )
            } else {
                asset

            }

        }

        // 保存更新后的资产列表

        assetRepository.saveAssets(updatedAssets)

        // 记录操作历史
        val history = AssetHistory(
            id = System.currentTimeMillis(),
            assetId = params.assetId,
            oldValue = oldAsset.value,
            newValue = params.value,
            operationType = OperationType.UPDATE.name

        )

        assetRepository.addAssetHistory(history)

    }
}