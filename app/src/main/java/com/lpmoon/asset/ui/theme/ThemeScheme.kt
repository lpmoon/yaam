package com.lpmoon.asset.ui.theme

enum class ThemeScheme(
    val displayName: String,
    val description: String
) {
    FRESH_PROFESSIONAL(
        displayName = "清新专业",
        description = "蓝色系为主，绿色点缀，专业与增长"
    ),
    SERIOUS_FINANCE(
        displayName = "沉稳金融",
        description = "深色调为主，稳重可靠的安全感"
    ),
    VIBRANT_MODERN(
        displayName = "活泼现代",
        description = "高饱和度颜色，充满活力"
    ),
    NATURAL_WARM(
        displayName = "自然温暖",
        description = "大地色系，温暖可信赖"
    ),
    MINIMAL_MONOCHROME(
        displayName = "简约单色",
        description = "灰色调为主，减少视觉干扰"
    ),
    HIGH_CONTRAST(
        displayName = "高对比度",
        description = "极致对比度，确保可访问性"
    )
}