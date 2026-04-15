package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.ExportAsset

/**
 * 文件导入资产用例
 * 负责处理导入资产数据的业务逻辑
 */
class FileImportAssetsUseCase : UseCase<FileImportAssetsUseCase.Params, FileImportAssetsUseCase.Result> {

    data class Params(
        // 导入数据（JSON字符串）
        val importData: String,
        // 导入源信息（如文件名、格式等）
        val importInfo: ImportInfo
    )

    data class ImportInfo(
        val fileName: String? = null,
        val format: ImportFormat = ImportFormat.JSON
    )

    enum class ImportFormat {
        JSON
    }

    data class Result(
        val success: Boolean,
        val importedAssets: List<Asset>? = null,
        val errorMessage: String? = null
    )

    override suspend fun invoke(params: Params): Result {
        return try {
            val gson = com.google.gson.Gson()
            val type = com.google.gson.reflect.TypeToken.getParameterized(
                List::class.java,
                ExportAsset::class.java
            ).type
            val exportAssets = gson.fromJson<List<ExportAsset>>(params.importData, type)

            if (exportAssets == null || exportAssets.isEmpty()) {
                return Result(
                    success = false,
                    errorMessage = "导入数据为空或格式不正确"
                )
            }

            // 转换为领域层资产模型
            val importedAssets = exportAssets.map { exportAsset ->
                Asset(
                    id = 0, // 新ID，由Repository分配
                    name = exportAsset.name,
                    value = exportAsset.value,
                    currency = exportAsset.currency ?: "CNY",
                    type = exportAsset.type ?: "OTHER"
                )
            }

            Result(
                success = true,
                importedAssets = importedAssets
            )
        } catch (e: Exception) {
            Result(
                success = false,
                errorMessage = "导入失败: ${e.message}"
            )
        }
    }
}