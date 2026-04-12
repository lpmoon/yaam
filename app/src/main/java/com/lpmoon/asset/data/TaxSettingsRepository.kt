package com.lpmoon.asset.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 税率设置数据仓库
 * 负责 SharedPreferences 的读写操作
 */
class TaxSettingsRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 加载税率设置
     */
    fun loadSettings(): TaxSettings {
        // 兼容处理：尝试读取 String，如果失败则读取旧版 Float
        val socialSecurityRate = try {
            sharedPreferences.getString(KEY_SOCIAL_SECURITY_RATE, null)?.toDoubleOrNull() ?: DEFAULT_SOCIAL_SECURITY_RATE
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_SOCIAL_SECURITY_RATE, DEFAULT_SOCIAL_SECURITY_RATE.toFloat()).toDouble()
        }

        val housingFundRate = try {
            sharedPreferences.getString(KEY_HOUSING_FUND_RATE, null)?.toDoubleOrNull() ?: DEFAULT_HOUSING_FUND_RATE
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_HOUSING_FUND_RATE, DEFAULT_HOUSING_FUND_RATE.toFloat()).toDouble()
        }

        val medicalInsuranceRate = try {
            sharedPreferences.getString(KEY_MEDICAL_INSURANCE_RATE, null)?.toDoubleOrNull() ?: DEFAULT_MEDICAL_INSURANCE_RATE
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_MEDICAL_INSURANCE_RATE, DEFAULT_MEDICAL_INSURANCE_RATE.toFloat()).toDouble()
        }

        val unemploymentInsuranceRate = try {
            sharedPreferences.getString(KEY_UNEMPLOYMENT_INSURANCE_RATE, null)?.toDoubleOrNull() ?: DEFAULT_UNEMPLOYMENT_INSURANCE_RATE
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_UNEMPLOYMENT_INSURANCE_RATE, DEFAULT_UNEMPLOYMENT_INSURANCE_RATE.toFloat()).toDouble()
        }

        val specialDeduction = try {
            sharedPreferences.getString(KEY_SPECIAL_DEDUCTION, null)?.toDoubleOrNull() ?: DEFAULT_SPECIAL_DEDUCTION
        } catch (e: Exception) {
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
     * 保存税率设置
     */
    fun saveSettings(settings: TaxSettings) {
        sharedPreferences.edit().apply {
            putString(KEY_SOCIAL_SECURITY_RATE, settings.socialSecurityRate.toString())
            putString(KEY_HOUSING_FUND_RATE, settings.housingFundRate.toString())
            putString(KEY_MEDICAL_INSURANCE_RATE, settings.medicalInsuranceRate.toString())
            putString(KEY_UNEMPLOYMENT_INSURANCE_RATE, settings.unemploymentInsuranceRate.toString())
            putString(KEY_SPECIAL_DEDUCTION, settings.specialDeduction.toString())
            apply()
        }
    }

    /**
     * 清除所有设置
     */
    fun clearSettings() {
        sharedPreferences.edit().clear().apply()
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
