package com.lpmoon.asset.domain.repository.tax

import com.lpmoon.asset.domain.model.tax.IncomeTaxHistory
import kotlinx.coroutines.flow.Flow

/**
 * 普通收入计算器历史记录仓库接口
 */
interface IncomeTaxHistoryRepository {
    fun getHistories(): Flow<List<IncomeTaxHistory>>
    suspend fun saveHistory(history: IncomeTaxHistory)
    suspend fun deleteHistory(historyId: Long)
    suspend fun clearAllHistories()
}
