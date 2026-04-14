package com.lpmoon.asset.data.asset

import java.text.SimpleDateFormat
import java.util.*

/**
 * 总资产快照
 * @param timestamp 时间戳（毫秒）
 * @param totalValue 总资产值（人民币）
 */
data class TotalAssetSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val totalValue: Double
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getDateOnly(): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getYear(): String {
        val sdf = SimpleDateFormat("yyyy年", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getWeekOfYear(): String {
        val calendar = Calendar.getInstance(Locale.CHINA)
        calendar.time = Date(timestamp)
        val year = calendar.get(Calendar.YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return "${year}年第${week}周"
    }
}

/**
 * 时间维度
 */
enum class TimeDimension {
    DAY,    // 天维度
    WEEK,   // 周维度
    MONTH,  // 月维度
    YEAR    // 年维度
}