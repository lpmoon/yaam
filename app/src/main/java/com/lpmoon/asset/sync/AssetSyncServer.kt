package com.lpmoon.asset.sync

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.lpmoon.asset.data.asset.Asset
import com.lpmoon.asset.util.AssetImportExportService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.http.*
import java.net.NetworkInterface
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 资产同步服务器
 * 负责启动临时HTTP服务器，提供加密的资产数据
 */
class AssetSyncServer(private val context: Context) {

    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private var port: Int = 0
    private var sessionId: String = ""
    private var encryptionKey: ByteArray = ByteArray(0)
    private var assetsJson: String = ""

    private val gson = Gson()

    companion object {
        private const val TAG = "AssetSyncServer"
        private const val AES_ALGORITHM = "AES"
        private const val AES_TRANSFORMATION = "AES/ECB/PKCS5Padding"
        private const val SESSION_ID_LENGTH = 16
        private const val KEY_LENGTH = 16 // AES-128
    }

    /**
     * 启动同步服务器
     * @param assets 要同步的资产列表
     * @return 包含服务器信息的SyncInfo对象，如果启动失败返回null
     */
    fun startServer(assets: List<Asset>): SyncInfo? {
        return try {
            // 生成会话ID和加密密钥
            sessionId = generateRandomString(SESSION_ID_LENGTH)
            encryptionKey = generateRandomKey()

            // 准备资产数据
            val exportData = assets.map { asset ->
                AssetImportExportService.ExportAsset(
                    name = asset.name,
                    type = asset.type,
                    value = asset.value,
                    currency = asset.currency
                )
            }
            assetsJson = gson.toJson(exportData)

            // 获取本地IP地址
            val ipAddress = getLocalIpAddress() ?: return null

            // 启动HTTP服务器
            port = findAvailablePort()
            server = embeddedServer(Netty, port = port) {
                install(ContentNegotiation) {
                    gson()
                }
                routing {
                    get("/") {
                        call.respondText("Asset Sync Server Ready", ContentType.Text.Plain)
                    }

                    get("/info") {
                        val info = mapOf(
                            "sessionId" to sessionId,
                            "timestamp" to System.currentTimeMillis(),
                            "dataSize" to assetsJson.length
                        )
                        call.respond(info)
                    }

                    get("/data") {
                        // 验证会话ID
                        val requestSessionId = call.request.queryParameters["sessionId"]
                        if (requestSessionId != sessionId) {
                            call.respond(HttpStatusCode.Forbidden, "Invalid session ID")
                            return@get
                        }

                        // 加密数据
                        val encryptedData = encryptData(assetsJson)
                        val response = mapOf(
                            "data" to Base64.encodeToString(encryptedData, Base64.DEFAULT),
                            "hash" to calculateHash(assetsJson)
                        )
                        call.respond(response)
                    }
                }
            }.start(wait = false)

            // 返回同步信息
            SyncInfo(
                serverAddress = "http://$ipAddress:$port",
                sessionId = sessionId,
                encryptionKey = Base64.encodeToString(encryptionKey, Base64.DEFAULT),
                timestamp = System.currentTimeMillis(),
                dataHash = calculateHash(assetsJson)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 停止服务器
     */
    fun stopServer() {
        server?.stop(1000, 5000)
        server = null
    }

    /**
     * 检查服务器是否正在运行
     */
    fun isRunning(): Boolean {
        return server != null
    }

    /**
     * 获取本地IP地址（非127.0.0.1）
     */
    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                // 跳过回环接口和未启用的接口
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    // 跳过IPv6和回环地址
                    if (address.isLoopbackAddress || address.hostAddress?.contains(":") == true) continue

                    return address.hostAddress
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 查找可用端口
     */
    private fun findAvailablePort(startPort: Int = 8080): Int {
        for (port in startPort..startPort + 100) {
            try {
                val socket = java.net.ServerSocket(port)
                socket.close()
                return port
            } catch (e: Exception) {
                // 端口被占用，继续尝试
            }
        }
        throw IllegalStateException("No available port found")
    }

    /**
     * 生成随机字符串
     */
    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     * 生成随机AES密钥
     */
    private fun generateRandomKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH)
        SecureRandom().nextBytes(key)
        return key
    }

    /**
     * 加密数据
     */
    private fun encryptData(data: String): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val keySpec = SecretKeySpec(encryptionKey, AES_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        return cipher.doFinal(data.toByteArray(Charsets.UTF_8))
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
     * 解密数据（用于客户端）
     */
    fun decryptData(encryptedData: ByteArray, key: ByteArray): String? {
        return try {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val keySpec = SecretKeySpec(key, AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decrypted = cipher.doFinal(encryptedData)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 同步信息数据类
     */
    data class SyncInfo(
        val serverAddress: String,
        val sessionId: String,
        val encryptionKey: String,
        val timestamp: Long,
        val dataHash: String
    )
}

