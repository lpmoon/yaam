package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.model.asset.OperationType
import com.lpmoon.asset.domain.model.asset.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 添加资产用例
 */
class AddAssetUseCase(
    private val assetRepository: AssetRepository,
    private val calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase
) : UseCase<AddAssetUseCase.Params, Unit> {

    data class Params(
        val name: String,
        val value: String,
        val currency: String,
        val type: String
    )

    override suspend fun invoke(params: Params) {
        val newAsset = Asset(
            id = 0, // Room autoGenerate 会自动处理
            name = params.name,
            value = params.value,
            currency = params.currency,
            type = params.type
        )

        val generatedId = assetRepository.addAsset(newAsset)

        val assetWithId = newAsset.copy(id = generatedId)

        // 记录操作历史
        val history = AssetHistory(
            id = System.currentTimeMillis(),
            assetId = assetWithId.id,
            oldValue = "",
            newValue = assetWithId.value,
            operationType = OperationType.CREATE
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
