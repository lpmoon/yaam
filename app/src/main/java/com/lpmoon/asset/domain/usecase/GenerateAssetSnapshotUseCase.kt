package com.lpmoon.asset.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.lpmoon.asset.data.asset.AssetType
import com.lpmoon.asset.domain.model.Asset
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 生成资产快照用例
 * 负责将资产数据渲染为位图的业务逻辑
 */
class GenerateAssetSnapshotUseCase : UseCase<GenerateAssetSnapshotUseCase.Params, GenerateAssetSnapshotUseCase.Result> {

    data class Params(
        val context: Context,
        val assets: List<Asset>,
        val totalAssets: Double,
        val getAssetValueInCny: (Asset) -> Double
    )

    data class Result(
        val success: Boolean,
        val bitmap: Bitmap? = null,
        val errorMessage: String? = null
    )

    /** 图片宽度（px），约等于 1080p 屏幕宽度 */
    private companion object {
        const val IMAGE_WIDTH = 1080
        const val PADDING = 48f
        const val CORNER_RADIUS = 24f
        val fmt = DecimalFormat("#,##0.00")
    }

    override suspend fun invoke(params: Params): Result {
        return try {
            val bitmap = renderAssetsBitmap(
                context = params.context,
                assets = params.assets,
                totalAssets = params.totalAssets,
                getAssetValueInCny = params.getAssetValueInCny
            )
            Result(
                success = true,
                bitmap = bitmap
            )
        } catch (e: Exception) {
            Result(
                success = false,
                errorMessage = "生成资产快照失败: ${e.message}"
            )
        }
    }

    /**
     * 用原生 Canvas API 绘制完整资产快照 Bitmap。
     * 所有尺寸单位均为像素（基于 [IMAGE_WIDTH]）。
     */
    private fun renderAssetsBitmap(
        context: Context,
        assets: List<Asset>,
        totalAssets: Double,
        getAssetValueInCny: (Asset) -> Double
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        // 把 dp 转成与 IMAGE_WIDTH 等比例的 px（IMAGE_WIDTH ≈ 360dp * 3）
        val scale = IMAGE_WIDTH / (360f * density)   // 相对于屏幕 dp 的缩放系数

        fun sp(dp: Float) = dp * density * scale

        // ---- 颜色（简单使用固定色，与 Theme 的 primaryContainer 接近）----
        val colorBackground    = Color.parseColor("#F6F6F6")
        val colorCard          = Color.parseColor("#D0E4FF")   // primaryContainer（蓝紫色系）
        val colorOnCard        = Color.parseColor("#001D36")   // onPrimaryContainer
        val colorSurface       = Color.WHITE
        val colorOnSurface     = Color.parseColor("#1A1C1E")
        val colorPrimary       = Color.parseColor("#005FAF")
        val colorSubtext       = Color.parseColor("#74777F")
        val colorGroupAmount   = Color.parseColor("#9E9E9E")
        val colorDivider       = Color.parseColor("#E0E0E0")

        // ---- Paint 定义 ----
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorBackground }
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorCard }
        val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorSurface }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorDivider; strokeWidth = 1f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorOnCard; textSize = sp(14f)
        }
        val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorOnCard; textSize = sp(28f); typeface = Typeface.DEFAULT_BOLD
        }
        val groupTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorOnSurface; textSize = sp(13f); typeface = Typeface.DEFAULT_BOLD
        }
        val groupAmountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorGroupAmount; textSize = sp(13f); typeface = Typeface.DEFAULT_BOLD
        }
        val assetNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorOnSurface; textSize = sp(13f); typeface = Typeface.DEFAULT_BOLD
        }
        val assetValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorPrimary; textSize = sp(13f); typeface = Typeface.DEFAULT_BOLD
        }
        val timestampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSubtext; textSize = sp(10f)
        }

        val contentWidth = IMAGE_WIDTH - PADDING * 2   // 内容区宽度

        // ---- 第一遍：计算总高度 ----
        // 使用AssetType枚举进行分组
        val groupedAssets = assets.groupBy { asset -> AssetType.fromString(asset.type) }
        val orderedTypes = AssetType.entries.filter { groupedAssets[it]?.isNotEmpty() == true }

        val cardHeight = PADDING + sp(14f) + sp(8f) + sp(28f) + PADDING  // 总资产卡片高度
        val rowHeight = sp(13f) + PADDING * 0.8f                          // 每个资产行高度
        val groupHeaderHeight = sp(13f) + PADDING * 0.6f                  // 分组标题高度
        val groupSpacing = PADDING * 0.3f

        var totalHeight = PADDING                                           // 顶部间距
        totalHeight += cardHeight + PADDING                                 // 卡片
        // 各分组
        for (type in orderedTypes) {
            totalHeight += groupHeaderHeight
            totalHeight += (groupedAssets[type]?.size ?: 0) * rowHeight
            totalHeight += groupSpacing
        }
        // 时间戳行
        val tsHeight = sp(10f) + PADDING
        totalHeight += tsHeight + PADDING                                   // 底部间距

        // ---- 创建 Bitmap ----
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, totalHeight.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(colorBackground)

        var y = PADDING

        // ---- 总资产卡片 ----
        val cardRect = RectF(PADDING, y, IMAGE_WIDTH - PADDING, y + cardHeight)
        canvas.drawRoundRect(cardRect, CORNER_RADIUS, CORNER_RADIUS, cardPaint)

        // 标题
        y += PADDING
        canvas.drawText("总资产 (人民币)", PADDING * 2, y + sp(14f), titlePaint)
        y += sp(14f) + sp(8f)

        // 金额
        canvas.drawText("¥${fmt.format(totalAssets)}", PADDING * 2, y + sp(28f), amountPaint)
        y += sp(28f) + PADDING   // 卡片结束

        y += PADDING   // 卡片与列表间距

        // ---- 资产分组列表（白色卡片背景）----
        if (assets.isNotEmpty()) {
            // 计算列表区高度
            var listHeight = 0f
            for (type in orderedTypes) {
                listHeight += groupHeaderHeight
                listHeight += (groupedAssets[type]?.size ?: 0) * rowHeight
                listHeight += groupSpacing
            }
            listHeight = listHeight.coerceAtLeast(1f)

            val listRect = RectF(PADDING, y, IMAGE_WIDTH - PADDING, y + listHeight)
            canvas.drawRoundRect(listRect, CORNER_RADIUS, CORNER_RADIUS, surfacePaint)

            for (type in orderedTypes) {
                val group = groupedAssets[type] ?: continue
                val groupTotal = group.sumOf { getAssetValueInCny(it) }

                // 分组标题行
                val groupY = y + groupHeaderHeight * 0.7f
                canvas.drawText(type.displayName, PADDING * 2, groupY + sp(13f), groupTitlePaint)
                val groupAmountStr = fmt.format(groupTotal)
                val gaWidth = groupAmountPaint.measureText(groupAmountStr)
                canvas.drawText(groupAmountStr, IMAGE_WIDTH - PADDING * 2 - gaWidth, groupY + sp(13f), groupAmountPaint)
                y += groupHeaderHeight

                // 分隔线
                canvas.drawLine(PADDING * 2, y, IMAGE_WIDTH - PADDING * 2, y, dividerPaint)

                // 资产条目行
                for (asset in group) {
                    val cny = getAssetValueInCny(asset)
                    val assetY = y + rowHeight * 0.65f
                    canvas.drawText(asset.name, PADDING * 2, assetY + sp(13f), assetNamePaint)
                    val valueStr = fmt.format(cny)
                    val valWidth = assetValuePaint.measureText(valueStr)
                    canvas.drawText(valueStr, IMAGE_WIDTH - PADDING * 2 - valWidth, assetY + sp(13f), assetValuePaint)
                    y += rowHeight
                }

                y += groupSpacing
            }
        }

        // ---- 时间戳 ----
        y += PADDING * 0.5f
        val tsStr = "生成时间：${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}"
        val tsWidth = timestampPaint.measureText(tsStr)
        canvas.drawText(tsStr, IMAGE_WIDTH - PADDING - tsWidth, y + sp(10f), timestampPaint)

        return bitmap
    }
}