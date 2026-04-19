package com.lpmoon.asset.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lpmoon.asset.data.local.room.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 资产数据访问对象
 */
@Dao
interface AssetDao {

    @Query("SELECT * FROM assets ORDER BY id ASC")
    fun getAllAssetsFlow(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets ORDER BY id ASC")
    suspend fun getAllAssets(): List<AssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets")
    suspend fun deleteAllAssets()

    @Query("SELECT COUNT(*) FROM assets")
    fun getAssetCountFlow(): Flow<Int>
}

