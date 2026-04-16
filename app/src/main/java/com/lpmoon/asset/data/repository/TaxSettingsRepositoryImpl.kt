package com.lpmoon.asset.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.lpmoon.asset.data.local.room.AppDatabase
import com.lpmoon.asset.data.local.room.entity.TaxSettingsEntity
import com.lpmoon.asset.domain.model.tax.TaxSettings
import com.lpmoon.asset.domain.repository.tax.TaxSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * 税率设置数据仓库实现
 * 负责 Room 数据库的读写操作，支持从 SharedPreferences 迁移
 */
class TaxSettingsRepositoryImpl(context: Context) : TaxSettingsRepository {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val taxSettingsDao = AppDatabase.getInstance(context).taxSettingsDao()

    // 初始化时检查并执行迁移
    init {
        runBlocking {
            migrateFromSharedPreferencesIfNeeded()
        }
    }

    override fun getTaxSettings(): Flow<TaxSettings> {
        return taxSettingsDao.getTaxSettingsFlow()
            .map { entity ->
                entity?.toDomainModel() ?: TaxSettings()
            }
    }

    /**
     * 从 SharedPreferences 迁移数据（如果需要）
     */
    private suspend fun migrateFromSharedPreferencesIfNeeded() {
        val existing = taxSettingsDao.getTaxSettings()
        if (existing == null) {
            val hasSharedPrefsData = sharedPreferences.contains(KEY_SOCIAL_SECURITY_RATE)
            if (hasSharedPrefsData) {
                val settings = loadFromSharedPreferences()
                saveSettings(settings)
                sharedPreferences.edit().clear().apply()
            }
        }
    }

    /**
     * 从 SharedPreferences 加载旧数据（用于迁移）
     */
    private fun loadFromSharedPreferences(): TaxSettings {
        // 兼容处理：尝试读取 String，如果失败则读取旧版 Float
        val socialSecurityRate = try {
            sharedPreferences.getString(KEY_SOCIAL_SECURITY_RATE, null)?.toDoubleOrNull() ?: DEFAULT_SOCIAL_SECURITY_RATE
        } catch (_: Exception) {
            sharedPreferences.getFloat(KEY_SOCIAL_SECURITY_RATE, DEFAULT_SOCIAL_SECURITY_RATE.toFloat()).toDouble()
        }

        val housingFundRate = try {
            sharedPreferences.getString(KEY_HOUSING_FUND_RATE, null)?.toDoubleOrNull() ?: DEFAULT_HOUSING_FUND_RATE
        } catch (_: Exception) {
            sharedPreferences.getFloat(KEY_HOUSING_FUND_RATE, DEFAULT_HOUSING_FUND_RATE.toFloat()).toDouble()
        }

        val medicalInsuranceRate = try {
            sharedPreferences.getString(KEY_MEDICAL_INSURANCE_RATE, null)?.toDoubleOrNull() ?: DEFAULT_MEDICAL_INSURANCE_RATE
        } catch (_: Exception) {
            sharedPreferences.getFloat(KEY_MEDICAL_INSURANCE_RATE, DEFAULT_MEDICAL_INSURANCE_RATE.toFloat()).toDouble()
        }

        val unemploymentInsuranceRate = try {
            sharedPreferences.getString(KEY_UNEMPLOYMENT_INSURANCE_RATE, null)?.toDoubleOrNull() ?: DEFAULT_UNEMPLOYMENT_INSURANCE_RATE
        } catch (_: Exception) {
            sharedPreferences.getFloat(KEY_UNEMPLOYMENT_INSURANCE_RATE, DEFAULT_UNEMPLOYMENT_INSURANCE_RATE.toFloat()).toDouble()
        }

        val specialDeduction = try {
            sharedPreferences.getString(KEY_SPECIAL_DEDUCTION, null)?.toDoubleOrNull() ?: DEFAULT_SPECIAL_DEDUCTION
        } catch (_: Exception) {
            sharedPreferences.getFloat(KEY_SPECIAL_DEDUCTION, DEFAULT_SPECIAL_DEDUCTION.toFloat()).toDouble()
        }

        return TaxSettings(
            socialSecurityRate = socialSecurityRate,
            housingFundRate = housingFundRate,
            medicalInsuranceRate = medicalInsuranceRate,
            unemploymentInsuranceRate = unemploymentInsuranceRate,
            specialDeduction = specialDeduction
        )
    }

    /**
     * 加载税率设置
     */
    override suspend fun loadSettings(): TaxSettings {
        return taxSettingsDao.getTaxSettings()?.toDomainModel() ?: TaxSettings()
    }

    /**
     * 保存税率设置
     */
    override suspend fun saveSettings(settings: TaxSettings) {
        val entity = TaxSettingsEntity.fromDomainModel(settings)
        taxSettingsDao.saveTaxSettings(entity)
    }

    /**
     * 清除所有设置
     */
    override suspend fun clearSettings() {
        taxSettingsDao.clearSettings()
    }

    companion object {
        private const val PREFS_NAME = "tax_settings"
        private const val KEY_SOCIAL_SECURITY_RATE = "social_security_rate"
        private const val KEY_HOUSING_FUND_RATE = "housing_fund_rate"
        private const val KEY_MEDICAL_INSURANCE_RATE = "medical_insurance_rate"
        private const val KEY_UNEMPLOYMENT_INSURANCE_RATE = "unemployment_insurance_rate"
        private const val KEY_SPECIAL_DEDUCTION = "special_deduction"

        // 默认值
        private const val DEFAULT_SOCIAL_SECURITY_RATE = 0.08
        private const val DEFAULT_HOUSING_FUND_RATE = 0.12
        private const val DEFAULT_MEDICAL_INSURANCE_RATE = 0.02
        private const val DEFAULT_UNEMPLOYMENT_INSURANCE_RATE = 0.005
        private const val DEFAULT_SPECIAL_DEDUCTION = 0.0
    }
}
