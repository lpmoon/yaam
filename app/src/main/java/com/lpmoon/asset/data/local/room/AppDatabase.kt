package com.lpmoon.asset.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lpmoon.asset.data.local.room.dao.AssetDao
import com.lpmoon.asset.data.local.room.dao.AssetHistoryDao
import com.lpmoon.asset.data.local.room.dao.ExchangeRateDao
import com.lpmoon.asset.data.local.room.dao.TotalAssetSnapshotDao
import com.lpmoon.asset.data.local.room.entity.AssetEntity
import com.lpmoon.asset.data.local.room.entity.AssetHistoryEntity
import com.lpmoon.asset.data.local.room.entity.ExchangeRateEntity
import com.lpmoon.asset.data.local.room.entity.TotalAssetSnapshotEntity

/**
 * Room 数据库
 */
@Database(
    entities = [
        AssetEntity::class,
        AssetHistoryEntity::class,
        TotalAssetSnapshotEntity::class,
        ExchangeRateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun assetDao(): AssetDao
    abstract fun assetHistoryDao(): AssetHistoryDao
    abstract fun totalAssetSnapshotDao(): TotalAssetSnapshotDao
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        private const val DATABASE_NAME = "asset_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

