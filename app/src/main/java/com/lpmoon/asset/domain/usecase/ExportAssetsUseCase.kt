package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset

/**
 * 导出资产用例
 * 负责准备导出资产数据的业务逻辑
 */
class ExportAssetsUseCase : UseCase<ExportAssetsUseCase.Params, ExportAssetsUseCase.Result> {

    data class Params(
        val assets: List<Asset>,
        // 导出目标信息（如文件名、格式等），不包含平台特定的Uri
        val exportInfo: ExportInfo
    )

    data class ExportInfo(
        val fileName: String? = null,
        val format: ExportFormat = ExportFormat.JSON
    )

    enum class ExportFormat {
        JSON
    }

    data class Result(
        val success: Boolean,
        val exportData: ExportData? = null,
        val errorMessage: String? = null
    )

    data class ExportData(
        val data: String, // JSON格式的资产数据
        val fileName: String,
        val format: ExportFormat
    )

    override suspend fun invoke(params: Params): Result {
        return try {
            // 准备导出数据
            val exportAssets = params.assets.map { asset ->
                ExportAsset(
                    name = asset.name,
                    type = asset.type,
                    value = asset.value,
                    currency = asset.currency
                )
            }

            val gson = com.google.gson.Gson()
            val json = gson.toJson(exportAssets)

            val fileName = params.exportInfo.fileName ?: generateDefaultFileName()

            Result(
                success = true,
                exportData = ExportData(
                    data = json,
                    fileName = fileName,
                    format = params.exportInfo.format
                )
            )
        } catch (e: Exception) {
            Result(
                success = false,
                errorMessage = "导出失败: ${e.message}"
            )
        }
    }

    private fun generateDefaultFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "assets_$timestamp.json"
    }

    data class ExportAsset(
        val name: String,
        val type: String,
        val value: String,
        val currency: String
    )
}