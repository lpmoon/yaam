package com.lpmoon.asset.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lpmoon.asset.data.local.room.dao.AssetDao
import com.lpmoon.asset.data.local.room.dao.AssetHistoryDao
import com.lpmoon.asset.data.local.room.dao.ExchangeRateDao
import com.lpmoon.asset.data.local.room.dao.IncomeTaxHistoryDao
import com.lpmoon.asset.data.local.room.dao.TaxSettingsDao
import com.lpmoon.asset.data.local.room.dao.TotalAssetSnapshotDao
import com.lpmoon.asset.data.local.room.entity.AssetEntity
import com.lpmoon.asset.data.local.room.entity.AssetHistoryEntity
import com.lpmoon.asset.data.local.room.entity.ExchangeRateEntity
import com.lpmoon.asset.data.local.room.entity.IncomeTaxHistoryEntity
import com.lpmoon.asset.data.local.room.entity.TaxSettingsEntity
import com.lpmoon.asset.data.local.room.entity.TotalAssetSnapshotEntity

/**
 * Room 数据库
 */
@Database(
    entities = [
        AssetEntity::class,
        AssetHistoryEntity::class,
        TotalAssetSnapshotEntity::class,
        ExchangeRateEntity::class,
        TaxSettingsEntity::class,
        IncomeTaxHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun assetDao(): AssetDao
    abstract fun assetHistoryDao(): AssetHistoryDao
    abstract fun totalAssetSnapshotDao(): TotalAssetSnapshotDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun taxSettingsDao(): TaxSettingsDao
    abstract fun incomeTaxHistoryDao(): IncomeTaxHistoryDao

    companion object {
        private const val DATABASE_NAME = "asset_database"

        // 从版本 3 到 4 的迁移：只添加新表，不删除现有数据
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS income_tax_histories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        calculationMode INTEGER NOT NULL,
                        inputSalary REAL NOT NULL,
                        socialSecurityRate REAL NOT NULL,
                        housingFundRate REAL NOT NULL,
                        medicalInsuranceRate REAL NOT NULL,
                        unemploymentInsuranceRate REAL NOT NULL,
                        specialDeduction REAL NOT NULL,
                        monthlySalary REAL NOT NULL,
                        socialSecurity REAL NOT NULL,
                        housingFund REAL NOT NULL,
                        medicalInsurance REAL NOT NULL,
                        unemploymentInsurance REAL NOT NULL,
                        totalInsurance REAL NOT NULL,
                        taxableIncome REAL NOT NULL,
                        taxRate REAL NOT NULL,
                        quickDeduction REAL NOT NULL,
                        incomeTax REAL NOT NULL,
                        afterTaxMonthly REAL NOT NULL,
                        afterTaxAnnual REAL NOT NULL,
                        actualTaxRate REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
