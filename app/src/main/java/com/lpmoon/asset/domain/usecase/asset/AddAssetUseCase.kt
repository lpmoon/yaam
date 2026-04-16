package com.lpmoon.asset.domain.usecase.asset



import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.model.asset.OperationType
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.UseCase
import kotlinx.coroutines.flow.first



/**
 * 添加资产用例
 */
class AddAssetUseCase(
    private val assetRepository: AssetRepository

) : UseCase<AddAssetUseCase.Params, Unit> {

    data class Params(
        val name: String,
        val value: String,
        val currency: String,
        val type: String
    )

    override suspend fun invoke(params: Params) {
        // 获取当前资产以确定下一个ID
        val currentAssets = assetRepository.getAllAssets().first()
        val nextId = if (currentAssets.isEmpty()) 1L else (currentAssets.maxOfOrNull { it.id } ?: 0) + 1

        val newAsset = Asset(
            id = nextId,
            name = params.name,
            value = params.value,
            currency = params.currency,
            type = params.type
        )

        // 保存资产
        assetRepository.saveAssets(currentAssets + newAsset)

        // 记录操作历史
        val history = AssetHistory(
            id = System.currentTimeMillis(),
            assetId = newAsset.id,
            oldValue = "",
            newValue = newAsset.value,
            operationType = OperationType.CREATE
        )
        assetRepository.addAssetHistory(history)
    }
}