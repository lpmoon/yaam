package com.lpmoon.asset.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.graphics.Bitmap
import android.content.ContentValues
import android.os.Environment
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 文件IO服务
 * 只包含平台特定的文件操作，不包含业务逻辑
 */
class FileIoService(private val context: Context) {

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
     * 从指定的Uri读取JSON字符串
     * @param uri JSON文件的Uri
     * @return JSON字符串，如果失败则返回null
     */
    fun readJsonFromUri(uri: Uri): String? {
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
     * 将Bitmap保存到系统图库
     * @param bitmap 要保存的位图
     * @param albumName 相册子目录名
     * @param fileNamePrefix 文件名前缀
     * @return 保存成功返回true，失败返回false
     */
    fun saveBitmapToGallery(
        bitmap: Bitmap,
        albumName: String = "资产管理",
        fileNamePrefix: String = "asset_snapshot"
    ): Boolean {
        val fileName = "${fileNamePrefix}_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        }.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$albumName"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, contentValues) ?: return false

        var outputStream: OutputStream? = null
        return try {
            outputStream = resolver.openOutputStream(uri)
            val saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            saved
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(uri, null, null)
            false
        } finally {
            outputStream?.close()
        }
    }

    /**
     * 生成默认的导出文件名
     * @param prefix 文件名前缀
     * @param extension 文件扩展名
     * @return 默认文件名，例如：assets_export_20250410_143022.json
     */
    fun generateDefaultFileName(
        prefix: String = "assets_export",
        extension: String = "json"
    ): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${prefix}_$timeStamp.$extension"
    }
}