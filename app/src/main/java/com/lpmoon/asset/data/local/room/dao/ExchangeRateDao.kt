package com.lpmoon.asset.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lpmoon.asset.data.local.room.entity.ExchangeRateEntity

/**
 * 汇率数据访问对象
 */
@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rate WHERE id = 1")
    suspend fun getExchangeRate(): ExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveExchangeRate(rate: ExchangeRateEntity)

    @Query("DELETE FROM exchange_rate")
    suspend fun clearCache()
}

