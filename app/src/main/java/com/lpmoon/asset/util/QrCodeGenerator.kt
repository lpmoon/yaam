package com.lpmoon.asset.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * 二维码生成工具
 */
object QrCodeGenerator {

    /**
     * 生成二维码位图
     * @param content 二维码内容
     * @param size 图片尺寸（像素）
     * @param margin 边距（模块数）
     * @return 生成的二维码ImageBitmap，如果生成失败返回null
     */
    fun generateQrCodeBitmap(content: String, size: Int = 600, margin: Int = 1): ImageBitmap? {
        return try {
            val hints = mutableMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            hints[EncodeHintType.MARGIN] = margin

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 生成带logo的二维码（可选功能）
     */
    fun generateQrCodeWithLogo(
        content: String,
        logo: Bitmap? = null,
        size: Int = 600,
        margin: Int = 1
    ): ImageBitmap? {
        val baseBitmap = generateQrCodeBitmap(content, size, margin) ?: return null

        if (logo == null) {
            return baseBitmap
        }

        // 这里可以添加logo叠加逻辑
        // 由于时间关系，暂不实现
        return baseBitmap
    }
}