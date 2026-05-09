package com.lpmoon.asset.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lpmoon.asset.data.local.room.entity.IncomeTaxHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 普通收入计算器历史记录数据访问对象
 */
@Dao
interface IncomeTaxHistoryDao {

    @Query("SELECT * FROM income_tax_histories ORDER BY timestamp DESC")
    fun getAllHistoriesFlow(): Flow<List<IncomeTaxHistoryEntity>>

    @Query("SELECT * FROM income_tax_histories ORDER BY timestamp DESC")
    suspend fun getAllHistories(): List<IncomeTaxHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: IncomeTaxHistoryEntity): Long

    @Query("DELETE FROM income_tax_histories WHERE id = :historyId")
    suspend fun deleteHistoryById(historyId: Long)

    @Query("DELETE FROM income_tax_histories")
    suspend fun deleteAllHistories()
}
