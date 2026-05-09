package com.lpmoon.asset.data.repository

import android.util.Log
import com.lpmoon.asset.data.local.room.AppDatabase
import com.lpmoon.asset.data.local.room.dao.IncomeTaxHistoryDao
import com.lpmoon.asset.data.local.room.entity.IncomeTaxHistoryEntity
import com.lpmoon.asset.domain.model.tax.IncomeTaxHistory
import com.lpmoon.asset.domain.repository.tax.IncomeTaxHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "IncomeTaxHistoryRepo"

/**
 * 普通收入计算器历史记录数据仓库实现
 */
class IncomeTaxHistoryRepositoryImpl(context: android.content.Context) : IncomeTaxHistoryRepository {
    private val incomeTaxHistoryDao: IncomeTaxHistoryDao = AppDatabase.getInstance(context).incomeTaxHistoryDao()

    override fun getHistories(): Flow<List<IncomeTaxHistory>> {
        Log.d(TAG, "getHistories called")
        return incomeTaxHistoryDao.getAllHistoriesFlow()
            .map { entityList ->
                Log.d(TAG, "Got ${entityList.size} histories from DB")
                entityList.map { it.toDomainModel() }
            }
    }

    override suspend fun saveHistory(history: IncomeTaxHistory) {
        Log.d(TAG, "saveHistory: ${history.getSalaryDisplay()}")
        try {
            val entity = IncomeTaxHistoryEntity.fromDomainModel(history)
            val id = incomeTaxHistoryDao.insertHistory(entity)
            Log.d(TAG, "saveHistory success, id=$id")
        } catch (e: Exception) {
            Log.e(TAG, "saveHistory failed", e)
        }
    }

    override suspend fun deleteHistory(historyId: Long) {
        Log.d(TAG, "deleteHistory: $historyId")
        try {
            incomeTaxHistoryDao.deleteHistoryById(historyId)
            Log.d(TAG, "deleteHistory success")
        } catch (e: Exception) {
            Log.e(TAG, "deleteHistory failed", e)
        }
    }

    override suspend fun clearAllHistories() {
        Log.d(TAG, "clearAllHistories")
        try {
            incomeTaxHistoryDao.deleteAllHistories()
            Log.d(TAG, "clearAllHistories success")
        } catch (e: Exception) {
            Log.e(TAG, "clearAllHistories failed", e)
        }
    }
}
