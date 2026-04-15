package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.TimeDimension
import com.lpmoon.asset.domain.repository.AssetRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * 计算资产历史用例
 */
class CalculateAssetHistoryUseCase(
    private val assetRepository: AssetRepository
) : UseCase<TimeDimension, List<Pair<String, Double>>> {

    override suspend fun invoke(dimension: TimeDimension): List<Pair<String, Double>> {
        val allSnapshots = assetRepository.getAllTotalAssetHistory().first()
            .sortedBy { it.timestamp }

        if (allSnapshots.isEmpty()) {
            return emptyList()
        }

        // 创建时间范围，从最早快照到当前时间
        val calendar = Calendar.getInstance(Locale.CHINA)
        // 从最早快照往前推5年，以便显示更早的历史数据
        val earliestTimestampCal = Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = allSnapshots.first().timestamp
            add(Calendar.YEAR, -5) // 往前推5年
        }
        val earliestTimestamp = earliestTimestampCal.timeInMillis
        val latestTimestamp = System.currentTimeMillis()

        // 生成时间序列键
        val timeKeys = mutableListOf<String>()
        val keyToValue = mutableMapOf<String, Double>()

        calendar.timeInMillis = earliestTimestamp
        val endCalendar = Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = latestTimestamp
        }

        // 根据维度调整时间到单位开始
        when (dimension) {
            TimeDimension.DAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeDimension.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.DAY_OF_WEEK, endCalendar.firstDayOfWeek)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeDimension.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeDimension.YEAR -> {
                calendar.set(Calendar.MONTH, 0)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endCalendar.set(Calendar.MONTH, 0)
                endCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)
                endCalendar.set(Calendar.MILLISECOND, 0)
            }
        }

        // 生成所有时间键
        while (calendar.timeInMillis <= endCalendar.timeInMillis) {
            val key = when (dimension) {
                TimeDimension.DAY -> {
                    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                    sdf.format(Date(calendar.timeInMillis))
                }
                TimeDimension.WEEK -> {
                    val year = calendar.get(Calendar.YEAR)
                    val week = calendar.get(Calendar.WEEK_OF_YEAR)
                    "${year}年第${week}周"
                }
                TimeDimension.MONTH -> {
                    val sdf = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
                    sdf.format(Date(calendar.timeInMillis))
                }
                TimeDimension.YEAR -> {
                    val sdf = SimpleDateFormat("yyyy年", Locale.CHINA)
                    sdf.format(Date(calendar.timeInMillis))
                }
            }
            timeKeys.add(key)

            // 移动到下一个时间单位
            when (dimension) {
                TimeDimension.DAY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                TimeDimension.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                TimeDimension.MONTH -> calendar.add(Calendar.MONTH, 1)
                TimeDimension.YEAR -> calendar.add(Calendar.YEAR, 1)
            }
        }

        // 将快照分配到时间键
        val snapshotGroups = allSnapshots.groupBy { snapshot ->
            when (dimension) {
                TimeDimension.DAY -> {
                    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                    sdf.format(Date(snapshot.timestamp))
                }
                TimeDimension.WEEK -> {
                    val cal = Calendar.getInstance(Locale.CHINA).apply {
                        timeInMillis = snapshot.timestamp
                    }
                    val year = cal.get(Calendar.YEAR)
                    val week = cal.get(Calendar.WEEK_OF_YEAR)
                    "${year}年第${week}周"
                }
                TimeDimension.MONTH -> {
                    val sdf = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
                    sdf.format(Date(snapshot.timestamp))
                }
                TimeDimension.YEAR -> {
                    val sdf = SimpleDateFormat("yyyy年", Locale.CHINA)
                    sdf.format(Date(snapshot.timestamp))
                }
            }
        }

        // 每个组取最后一个快照的值（时间戳最大的）
        snapshotGroups.forEach { (key, snapshots) ->
            val latestSnapshot = snapshots.maxByOrNull { it.timestamp }
            latestSnapshot?.let {
                keyToValue[key] = it.totalValue
            }
        }

        // 用前一个有效值填充缺失的时间键
        val result = mutableListOf<Pair<String, Double>>()
        var lastValidValue = 0.0

        for (key in timeKeys) {
            val value = keyToValue[key] ?: lastValidValue
            result.add(Pair(key, value))
            if (keyToValue[key] != null) {
                lastValidValue = value
            }
        }

        return result
    }
}