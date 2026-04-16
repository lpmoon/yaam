package com.lpmoon.asset.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lpmoon.asset.data.local.room.entity.TaxSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 税率设置数据访问对象
 */
@Dao
interface TaxSettingsDao {

    @Query("SELECT * FROM tax_settings WHERE id = 1")
    fun getTaxSettingsFlow(): Flow<TaxSettingsEntity?>

    @Query("SELECT * FROM tax_settings WHERE id = 1")
    suspend fun getTaxSettings(): TaxSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTaxSettings(settings: TaxSettingsEntity)

    @Query("DELETE FROM tax_settings")
    suspend fun clearSettings()
}
