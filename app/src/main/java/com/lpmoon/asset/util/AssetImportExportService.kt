package com.lpmoon.asset.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lpmoon.asset.data.asset.Asset
import com.lpmoon.asset.data.asset.AssetType
import com.lpmoon.asset.data.asset.CurrencyType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 资产导入导出服务
 */
class AssetImportExportService(private val context: Context) {

    private val gson = Gson()

    /**
     * 导出资产数据到指定的Uri（JSON文件）- 向后兼容方法
     * 注意：这个方法包含业务逻辑（创建ExportAsset列表），应该逐渐被弃用
     * @param assets 资产列表
     * @param uri 目标文件的Uri
     * @return 导出成功返回true，失败返回false
     */
    fun exportAssetsToUri(assets: List<Asset>, uri: Uri): Boolean {
        return try {
            // 创建导出数据（只包含必要字段）- 业务逻辑，应该移到UseCase中
            val exportData = assets.map { asset ->
                ExportAsset(
                    name = asset.name,
                    type = asset.type,
                    value = asset.value,
                    currency = asset.currency
                )
            }

            val json = gson.toJson(exportData)

            // 写入到提供的Uri（平台特定操作）
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
                outputStream.flush()
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 将JSON数据写入指定的Uri（纯文件操作，无业务逻辑）
     * @param json JSON字符串
     * @param uri 目标文件的Uri
     * @return 写入成功返回true，失败返回false
     */
    fun writeJsonToUri(json: String, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
                outputStream.flush()
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 生成默认的导出文件名
     * @return 默认文件名，例如：assets_export_20250410_143022.json
     */
    fun generateDefaultFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "assets_export_$timeStamp.json"
    }

    /**
     * 从指定的Uri读取JSON字符串
     * @param uri JSON文件的Uri
     * @return JSON字符串，如果失败则返回null
     */
    fun readAssetsJsonFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从指定的Uri导入资产数据（JSON文件）- 向后兼容方法
     * 注意：这个方法包含业务逻辑（JSON解析），应该逐渐被弃用
     * @param uri JSON文件的Uri
     * @return 导入的资产列表，如果失败则返回null
     */
    fun importAssetsFromUri(uri: Uri): List<Asset>? {
        return try {
            // 从Uri读取文件内容
            val json = readAssetsJsonFromUri(uri) ?: return null

            // 解析JSON（业务逻辑，应该移到UseCase中）
            val type = object : TypeToken<List<ExportAsset>>() {}.type
            val exportAssets = gson.fromJson<List<ExportAsset>>(json, type)

            // 转换为Asset对象（生成新的ID）
            exportAssets.map { exportAsset ->
                Asset(
                    id = 0, // 新ID，由Repository分配
                    name = exportAsset.name,
                    value = exportAsset.value,
                    currency = exportAsset.currency ?: CurrencyType.CNY.name,
                    type = exportAsset.type ?: AssetType.OTHER.name
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 导出用的资产数据类（只包含必要字段）
     */
    data class ExportAsset(
        val name: String,
        val type: String? = null,
        val value: String,
        val currency: String? = null
    )
}