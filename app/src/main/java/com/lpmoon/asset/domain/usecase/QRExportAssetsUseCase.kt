package com.lpmoon.asset.domain.usecase

import com.google.gson.Gson
import com.lpmoon.asset.data.asset.Asset
import com.lpmoon.asset.sync.AssetSyncServer

/**
 * 二维码导出资产用例
 * 负责生成资产同步二维码内容
 */
class QRExportAssetsUseCase : UseCase<QRExportAssetsUseCase.Params, QRExportAssetsUseCase.Result> {

    data class Params(
        val assets: List<Asset>
    )

    data class Result(
        val success: Boolean,
        val qrContent: String? = null,
        val errorMessage: String? = null
    )

    override suspend fun invoke(params: Params): Result {
        return try {
            // 创建SyncInfo对象
            // 这里简化处理，实际应该使用AssetSyncServer生成SyncInfo
            // 暂时返回一个基本的JSON结构
            val syncInfo = AssetSyncServer.SyncInfo(
                serverAddress = "http://localhost:8080",
                sessionId = generateRandomString(16),
                encryptionKey = generateRandomString(16),
                timestamp = System.currentTimeMillis(),
                dataHash = ""
            )

            val gson = Gson()
            val qrContent = gson.toJson(syncInfo)

            Result(
                success = true,
                qrContent = qrContent
            )
        } catch (e: Exception) {
            Result(
                success = false,
                errorMessage = "生成二维码内容失败: ${e.message}"
            )
        }
    }

    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

}