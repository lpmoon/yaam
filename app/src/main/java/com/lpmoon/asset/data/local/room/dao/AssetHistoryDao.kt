package com.lpmoon.asset.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lpmoon.asset.data.local.room.entity.AssetHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 资产操作记录数据访问对象
 */
@Dao
interface AssetHistoryDao {

    @Query("SELECT * FROM asset_histories WHERE assetId = :assetId ORDER BY timestamp DESC")
    fun getHistoriesByAssetIdFlow(assetId: Long): Flow<List<AssetHistoryEntity>>

    @Query("SELECT * FROM asset_histories ORDER BY timestamp DESC")
    fun getAllHistoriesFlow(): Flow<List<AssetHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AssetHistoryEntity): Long

    @Query("DELETE FROM asset_histories WHERE assetId = :assetId")
    suspend fun deleteHistoriesByAssetId(assetId: Long)

    @Query("DELETE FROM asset_histories WHERE assetId = :assetId AND id NOT IN (SELECT id FROM asset_histories WHERE assetId = :assetId ORDER BY timestamp DESC LIMIT 1)")
    suspend fun keepOnlyLastHistoryByAssetId(assetId: Long)

    @Query("DELETE FROM asset_histories")
    suspend fun deleteAllHistories()
}

