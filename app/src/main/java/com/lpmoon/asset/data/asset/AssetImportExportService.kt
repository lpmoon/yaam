package com.lpmoon.asset.data.asset

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * 资产导入导出服务
 */
class AssetImportExportService(private val context: Context) {

    private val gson = Gson()

    /**
     * 导出资产数据到指定的Uri（JSON文件）
     * @param assets 资产列表
     * @param uri 目标文件的Uri
     * @return 导出成功返回true，失败返回false
     */
    fun exportAssetsToUri(assets: List<Asset>, uri: Uri): Boolean {
        return try {
            // 创建导出数据（只包含必要字段）
            val exportData = assets.map { asset ->
                ExportAsset(
                    name = asset.name,
                    type = asset.type,
                    value = asset.value,
                    currency = asset.currency
                )
            }

            val json = gson.toJson(exportData)

            // 写入到提供的Uri
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
     * 从指定的Uri导入资产数据（JSON文件）
     * @param uri JSON文件的Uri
     * @return 导入的资产列表，如果失败则返回null
     */
    fun importAssetsFromUri(uri: Uri): List<Asset>? {
        return try {
            // 从Uri读取文件内容
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }

                // 解析JSON
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