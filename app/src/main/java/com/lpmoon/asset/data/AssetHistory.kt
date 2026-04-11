package com.lpmoon.asset.data

import java.text.SimpleDateFormat
import java.util.*

/**
 * 资产操作记录
 * @param id 记录ID
 * @param assetId 关联的资产ID
 * @param oldValue 旧值（修改前的资产值）
 * @param newValue 新值（修改后的资产值）
 * @param timestamp 操作时间戳（毫秒）
 * @param operationType 操作类型：CREATE, UPDATE, DELETE
 */
data class AssetHistory(
    val id: Long = 0,
    val assetId: Long,
    val oldValue: String,
    val newValue: String,
    val timestamp: Long = System.currentTimeMillis(),
    val operationType: String = OperationType.UPDATE.name
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getDescription(): String {
        val time = getFormattedTime()
        return when (operationType) {
            OperationType.CREATE.name -> "$time 创建资产，初始值为 $newValue"
            OperationType.UPDATE.name -> "$time 将资产值从 $oldValue 修改为 $newValue"
            OperationType.DELETE.name -> "$time 删除资产，最后值为 $oldValue"
            else -> "$time 操作资产"
        }
    }
}

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE
}