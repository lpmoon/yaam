package com.lpmoon.asset.ui.config

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import com.lpmoon.asset.data.TaxSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TaxSettingsManager {
    private const val PREFS_NAME = "tax_settings"
    private const val KEY_SOCIAL_SECURITY_RATE = "social_security_rate"
    private const val KEY_HOUSING_FUND_RATE = "housing_fund_rate"
    private const val KEY_MEDICAL_INSURANCE_RATE = "medical_insurance_rate"
    private const val KEY_UNEMPLOYMENT_INSURANCE_RATE = "unemployment_insurance_rate"
    private const val KEY_SPECIAL_DEDUCTION = "special_deduction"

    private val _taxSettings = MutableStateFlow(TaxSettings())
    val taxSettings: StateFlow<TaxSettings> = _taxSettings.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadSettings()
    }

    private fun loadSettings() {
        val sharedPreferences = prefs ?: return

        // 兼容处理：尝试读取 String，如果失败则读取旧版 Float
        val socialSecurityRate = try {
            sharedPreferences.getString(KEY_SOCIAL_SECURITY_RATE, null)?.toDoubleOrNull() ?: 0.08
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_SOCIAL_SECURITY_RATE, 0.08f).toDouble()
        }

        val housingFundRate = try {
            sharedPreferences.getString(KEY_HOUSING_FUND_RATE, null)?.toDoubleOrNull() ?: 0.12
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_HOUSING_FUND_RATE, 0.12f).toDouble()
        }

        val medicalInsuranceRate = try {
            sharedPreferences.getString(KEY_MEDICAL_INSURANCE_RATE, null)?.toDoubleOrNull() ?: 0.02
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_MEDICAL_INSURANCE_RATE, 0.02f).toDouble()
        }

        val unemploymentInsuranceRate = try {
            sharedPreferences.getString(KEY_UNEMPLOYMENT_INSURANCE_RATE, null)?.toDoubleOrNull() ?: 0.005
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_UNEMPLOYMENT_INSURANCE_RATE, 0.005f).toDouble()
        }

        val specialDeduction = try {
            sharedPreferences.getString(KEY_SPECIAL_DEDUCTION, null)?.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            sharedPreferences.getFloat(KEY_SPECIAL_DEDUCTION, 0f).toDouble()
        }

        val settings = TaxSettings(
            socialSecurityRate = socialSecurityRate,
            housingFundRate = housingFundRate,
            medicalInsuranceRate = medicalInsuranceRate,
            unemploymentInsuranceRate = unemploymentInsuranceRate,
            specialDeduction = specialDeduction
        )
        _taxSettings.value = settings
    }

    private fun saveSettings() {
        val sharedPreferences = prefs ?: return
        val editor = sharedPreferences.edit()
        editor.putString(KEY_SOCIAL_SECURITY_RATE, _taxSettings.value.socialSecurityRate.toString())
        editor.putString(KEY_HOUSING_FUND_RATE, _taxSettings.value.housingFundRate.toString())
        editor.putString(KEY_MEDICAL_INSURANCE_RATE, _taxSettings.value.medicalInsuranceRate.toString())
        editor.putString(KEY_UNEMPLOYMENT_INSURANCE_RATE, _taxSettings.value.unemploymentInsuranceRate.toString())
        editor.putString(KEY_SPECIAL_DEDUCTION, _taxSettings.value.specialDeduction.toString())
        editor.apply()
    }

    fun updateTaxSettings(settings: TaxSettings) {
        _taxSettings.value = settings
        saveSettings()
    }

    fun updateSocialSecurityRate(rate: Double) {
        _taxSettings.value = _taxSettings.value.copy(socialSecurityRate = rate)
        saveSettings()
    }

    fun updateHousingFundRate(rate: Double) {
        _taxSettings.value = _taxSettings.value.copy(housingFundRate = rate)
        saveSettings()
    }

    fun updateMedicalInsuranceRate(rate: Double) {
        _taxSettings.value = _taxSettings.value.copy(medicalInsuranceRate = rate)
        saveSettings()
    }

    fun updateUnemploymentInsuranceRate(rate: Double) {
        _taxSettings.value = _taxSettings.value.copy(unemploymentInsuranceRate = rate)
        saveSettings()
    }

    fun updateSpecialDeduction(amount: Double) {
        _taxSettings.value = _taxSettings.value.copy(specialDeduction = amount)
        saveSettings()
    }
}
