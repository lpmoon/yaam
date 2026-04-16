package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.model.asset.OperationType
import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase
import kotlinx.coroutines.flow.first

/**
 * 更新资产用例
 */
class UpdateAssetUseCase(
    private val assetRepository: AssetRepository,
    private val calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase
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
            // 资产不存在，可能是竞态条件，记录错误但不抛出异常
            return
        }

        val updatedAsset = oldAsset.copy(
            name = params.name,
            value = params.value,
            currency = params.currency,
            type = params.type
        )

        assetRepository.updateAsset(updatedAsset)

        // 记录操作历史
        val history = AssetHistory(
            id = System.currentTimeMillis(),
            assetId = params.assetId,
            oldValue = oldAsset.value,
            newValue = params.value,
            operationType = OperationType.UPDATE
        )
        assetRepository.addAssetHistory(history)

        // 创建总资产快照
        addTotalAssetSnapshot()
    }

    private suspend fun addTotalAssetSnapshot() {
        try {
            val totalValue = calculateTotalAssetsUseCase.calculateNow()
            val snapshot = TotalAssetSnapshot(
                timestamp = System.currentTimeMillis(),
                totalValue = totalValue
            )
            assetRepository.addTotalAssetSnapshot(snapshot)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
