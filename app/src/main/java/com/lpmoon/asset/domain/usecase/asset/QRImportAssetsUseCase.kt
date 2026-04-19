package com.lpmoon.asset.domain.usecase.asset

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import com.lpmoon.asset.domain.model.asset.ExportAsset
import com.lpmoon.asset.domain.usecase.UseCase
import com.lpmoon.asset.sync.AssetSyncClient
import com.lpmoon.asset.sync.AssetSyncServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 二维码导入资产用例
 * 负责解析二维码内容并下载资产数据
 */
class QRImportAssetsUseCase : UseCase<QRImportAssetsUseCase.Params, QRImportAssetsUseCase.Result> {

    data class Params(
        val context: Context,
        val qrContent: String
    )

    data class Result(
        val success: Boolean,
        val importedAssets: List<ExportAsset>? = null,
        val errorMessage: String? = null
    )

    override suspend fun invoke(params: Params): Result {
        return withContext(Dispatchers.IO) {
            try {
                val syncClient = AssetSyncClient(params.context)

                // 首先尝试解析为SyncInfo格式
                val syncInfo = syncClient.parseQrContent(params.qrContent)
                if (syncInfo != null) {
                    // 下载资产数据
                    val assets = downloadAssets(syncClient, syncInfo)
                    return@withContext Result(
                        success = true,
                        importedAssets = assets
                    )
                }

                // 如果解析SyncInfo失败，尝试解析为直接的资产数据格式
                try {
                    val gson = Gson()
                    val type = TypeToken.getParameterized(
                        List::class.java,
                        ExportAsset::class.java
                    ).type
                    val assets = gson.fromJson<List<ExportAsset>>(params.qrContent, type)

                    if (assets != null && assets.isNotEmpty()) {
                        // 确保字段有默认值
                        val validatedAssets = assets.map { asset ->
                            ExportAsset(
                                name = asset.name.ifEmpty { "" },
                                type = asset.type.ifEmpty { "OTHER" },
                                value = asset.value.ifEmpty { "0" },
                                currency = asset.currency.ifEmpty { "CNY" }
                            )
                        }
                        return@withContext Result(
                            success = true,
                            importedAssets = validatedAssets
                        )
                    }
                } catch (e: Exception) {
                    // 继续尝试其他格式
                }

                // 尝试解析为简单的资产数据格式（可能缺少某些字段）
                try {
                    val gson = Gson()
                    val mapList = gson.fromJson<List<Map<String, Any>>>(params.qrContent, object : TypeToken<List<Map<String, Any>>>() {}.type)

                    if (mapList != null && mapList.isNotEmpty()) {
                        val assets = mapList.mapNotNull { map ->
                            try {
                                val name = map["name"] as? String ?: ""
                                val type = map["type"] as? String ?: "OTHER"
                                val value = map["value"] as? String ?: map["value"]?.toString() ?: "0"
                                // 安全处理currency，处理JsonNull情况
                                val currency = when (val c = map["currency"]) {
                                    is String -> c
                                    is JsonPrimitive -> c.asString
                                    else -> "CNY"
                                }
                                ExportAsset(
                                    name = name,
                                    type = type,
                                    value = value,
                                    currency = currency
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (assets.isNotEmpty()) {
                            return@withContext Result(
                                success = true,
                                importedAssets = assets
                            )
                        }
                    }
                } catch (e: Exception) {
                    // 最终失败
                }

                return@withContext Result(
                    success = false,
                    errorMessage = "无法识别二维码格式。请确保扫描的是资产同步二维码。"
                )
            } catch (e: Exception) {
                Result(
                    success = false,
                    errorMessage = "导入失败: ${e.message}"
                )
            }
        }
    }

    private suspend fun downloadAssets(
        syncClient: AssetSyncClient,
        syncInfo: AssetSyncServer.SyncInfo
    ): List<ExportAsset> = suspendCancellableCoroutine { continuation ->
        syncClient.downloadAssets(syncInfo, object : AssetSyncClient.SyncCallback {
            override fun onSuccess(assets: List<ExportAsset>) {
                continuation.resume(assets)
            }

            override fun onFailure(errorMessage: String) {
                continuation.resumeWithException(RuntimeException(errorMessage))
            }
        })
    }
}