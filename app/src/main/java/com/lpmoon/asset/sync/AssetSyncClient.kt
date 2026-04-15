package com.lpmoon.asset.sync

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lpmoon.asset.domain.model.ExportAsset
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 资产同步客户端
 * 负责解析二维码信息，从服务器下载资产数据
 */
class AssetSyncClient(private val context: Context) {

    private val gson = Gson()
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "AssetSyncClient"
    }

    /**
     * 同步回调接口
     */
    interface SyncCallback {
        fun onSuccess(assets: List<ExportAsset>)
        fun onFailure(errorMessage: String)
    }

    /**
     * 解析二维码内容
     * @param qrContent 二维码扫描结果字符串
     * @return 解析后的SyncInfo对象，如果格式无效返回null
     */
    fun parseQrContent(qrContent: String): AssetSyncServer.SyncInfo? {
        return try {
            val map = gson.fromJson(qrContent, Map::class.java)

            // 验证必需字段，但允许更宽松的格式
            // 检查是否包含必要的同步信息
            val serverAddress = map["server"] as? String ?: map["serverAddress"] as? String
            val sessionId = map["sessionId"] as? String
            val encryptionKey = map["encryptionKey"] as? String
            val timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            val dataHash = map["dataHash"] as? String ?: ""

            // 如果缺少必需字段，尝试解析为其他格式
            if (serverAddress == null || sessionId == null || encryptionKey == null) {
                // 检查是否是资产数据格式
                val isAssetData = try {
                    val type = com.google.gson.reflect.TypeToken.getParameterized(
                        List::class.java,
                        com.lpmoon.asset.domain.model.ExportAsset::class.java
                    ).type
                    val assets = gson.fromJson<List<com.lpmoon.asset.domain.model.ExportAsset>>(qrContent, type)
                    assets != null && assets.isNotEmpty()
                } catch (e: Exception) {
                    false
                }

                if (isAssetData) {
                    Log.d(TAG, "QR contains direct asset data, not sync info")
                    return null
                }

                // 尝试从AssetSyncServer.SyncInfo对象解析
                try {
                    return gson.fromJson(qrContent, AssetSyncServer.SyncInfo::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "QR content is not valid sync info format")
                    return null
                }
            }

            AssetSyncServer.SyncInfo(
                serverAddress = serverAddress,
                sessionId = sessionId,
                encryptionKey = encryptionKey,
                timestamp = timestamp,
                dataHash = dataHash
            )
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse QR content as JSON", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR content", e)
            null
        }
    }

    /**
     * 从服务器下载资产数据
     * @param syncInfo 同步信息
     * @param callback 结果回调
     */
    fun downloadAssets(syncInfo: AssetSyncServer.SyncInfo, callback: SyncCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 验证时间戳（可选，防止重放攻击）
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - syncInfo.timestamp
                if (timeDiff > 5 * 60 * 1000) { // 5分钟有效期
                    withContext(Dispatchers.Main) {
                        callback.onFailure("二维码已过期，请重新生成")
                    }
                    return@launch
                }

                // 2. 获取服务器信息
                val infoResponse = fetchServerInfo(syncInfo)
                if (!infoResponse.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        callback.onFailure("无法连接服务器")
                    }
                    return@launch
                }

                // 3. 下载加密数据
                val dataResponse = fetchEncryptedData(syncInfo)
                if (!dataResponse.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        callback.onFailure("下载数据失败: ${dataResponse.code}")
                    }
                    return@launch
                }

                // 4. 解析响应
                val responseBody = dataResponse.body?.string() ?: ""
                val dataMap = gson.fromJson(responseBody, Map::class.java)
                val encryptedDataBase64 = dataMap["data"] as? String ?: throw IOException("No data field")
                val receivedHash = dataMap["hash"] as? String ?: throw IOException("No hash field")

                // 5. 解密数据
                val encryptedData = Base64.decode(encryptedDataBase64, Base64.DEFAULT)
                val encryptionKeyBytes = Base64.decode(syncInfo.encryptionKey, Base64.DEFAULT)
                val syncServer = AssetSyncServer(context)
                val decryptedJson = syncServer.decryptData(encryptedData, encryptionKeyBytes)
                    ?: throw IOException("解密失败")

                // 6. 验证数据完整性
                val calculatedHash = calculateHash(decryptedJson)
                if (calculatedHash != syncInfo.dataHash && calculatedHash != receivedHash) {
                    Log.w(TAG, "Hash mismatch: calculated=$calculatedHash, expected=${syncInfo.dataHash}, received=$receivedHash")
                    // 继续处理，因为可能只是服务器返回的hash与二维码不同
                }

                // 7. 解析资产数据
                val assets = parseAssets(decryptedJson)

                // 8. 回调成功
                withContext(Dispatchers.Main) {
                    callback.onSuccess(assets)
                }

            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                withContext(Dispatchers.Main) {
                    callback.onFailure("网络错误: ${e.message}")
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parsing error", e)
                withContext(Dispatchers.Main) {
                    callback.onFailure("数据格式错误")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                withContext(Dispatchers.Main) {
                    callback.onFailure("同步失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 获取服务器信息（验证连接）
     */
    private fun fetchServerInfo(syncInfo: AssetSyncServer.SyncInfo): Response {
        val request = Request.Builder()
            .url("${syncInfo.serverAddress}/info")
            .get()
            .build()

        return okHttpClient.newCall(request).execute()
    }

    /**
     * 下载加密数据
     */
    private fun fetchEncryptedData(syncInfo: AssetSyncServer.SyncInfo): Response {
        val url = "${syncInfo.serverAddress}/data?sessionId=${syncInfo.sessionId}"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return okHttpClient.newCall(request).execute()
    }

    /**
     * 计算数据哈希值（SHA-256）
     */
    private fun calculateHash(data: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.DEFAULT).trim()
    }

    /**
     * 解析资产JSON
     */
    private fun parseAssets(json: String): List<ExportAsset> {
        val type = com.google.gson.reflect.TypeToken.getParameterized(
            List::class.java,
            ExportAsset::class.java
        ).type

        return gson.fromJson(json, type)
    }

    /**
     * 快速检查服务器是否可达
     */
    suspend fun checkServerReachable(syncInfo: AssetSyncServer.SyncInfo): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${syncInfo.serverAddress}/")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}