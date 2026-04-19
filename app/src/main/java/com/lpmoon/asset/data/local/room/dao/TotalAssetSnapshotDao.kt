package com.lpmoon.asset.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lpmoon.asset.data.local.room.entity.TotalAssetSnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * 总资产快照数据访问对象
 */
@Dao
interface TotalAssetSnapshotDao {

    @Query("SELECT * FROM total_asset_snapshots ORDER BY timestamp ASC")
    fun getAllSnapshotsFlow(): Flow<List<TotalAssetSnapshotEntity>>

    @Query("SELECT * FROM total_asset_snapshots ORDER BY timestamp ASC")
    suspend fun getAllSnapshots(): List<TotalAssetSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: TotalAssetSnapshotEntity): Long

    @Query("DELETE FROM total_asset_snapshots")
    suspend fun deleteAllSnapshots()
}

