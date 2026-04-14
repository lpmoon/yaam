package com.lpmoon.asset.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lpmoon.asset.data.tax.TaxSettings
import com.lpmoon.asset.data.tax.TaxSettingsRepository
import com.lpmoon.asset.data.tax.formatNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 税率设置 ViewModel
 * 管理税率设置的状态、百分比转换逻辑
 */
class TaxSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaxSettingsRepository(application)

    private val _taxSettings = MutableStateFlow(repository.loadSettings())
    val taxSettings: StateFlow<TaxSettings> = _taxSettings.asStateFlow()

    init {
        // 在初始化时加载设置
        loadSettings()
    }

    /**
     * 加载税率设置
     */
    private fun loadSettings() {
        _taxSettings.value = repository.loadSettings()
    }

    /**
     * 更新所有税率设置
     */
    fun updateTaxSettings(settings: TaxSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            _taxSettings.value = settings
        }
    }

    /**
     * 更新养老保险比例（传入百分比，如 8 表示 8%）
     */
    fun updateSocialSecuritySecurityPercent(percent: Double) {
        val rate = percent / 100.0
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(socialSecurityRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新养老保险比例（传入小数，如 0.08 表示 8%）
     */
    fun updateSocialSecurityRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(socialSecurityRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新公积金比例（传入百分比，如 12 表示 12%）
     */
    fun updateHousingFundPercent(percent: Double) {
        val rate = percent / 100.0
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(housingFundRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新公积金比例（传入小数，如 0.12 表示 12%）
     */
    fun updateHousingFundRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(housingFundRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新医疗保险比例（传入百分比，如 2 表示 2%）
     */
    fun updateMedicalInsurancePercent(percent: Double) {
        val rate = percent / 100.0
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(medicalInsuranceRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新医疗保险比例（传入小数，如 0.02 表示 2%）
     */
    fun updateMedicalInsuranceRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(medicalInsuranceRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新失业保险比例（传入百分比，如 0.5 表示 0.5%）
     */
    fun updateUnemploymentInsurancePercent(percent: Double) {
        val rate = percent / 100.0
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(unemploymentInsuranceRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新失业保险比例（传入小数，如 0.005 表示 0.5%）
     */
    fun updateUnemploymentInsuranceRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(unemploymentInsuranceRate = rate)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 更新专项附加扣除
     */
    fun updateSpecialDeduction(amount: Double) {
        viewModelScope.launch {
            val newSettings = _taxSettings.value.copy(specialDeduction = amount)
            repository.saveSettings(newSettings)
            _taxSettings.value = newSettings
        }
    }

    /**
     * 格式化养老保险百分比为字符串（如 0.08 -> "8"）
     */
    fun formatSocialSecurityPercent(): String {
        return formatNumber(_taxSettings.value.socialSecurityRate * 100)
    }

    /**
     * 格式化公积金百分比为字符串（如 0.12 -> "12"）
     */
    fun formatHousingFundPercent(): String {
        return formatNumber(_taxSettings.value.housingFundRate * 100)
    }

    /**
     * 格式化医疗保险百分比为字符串（如 0.02 -> "2"）
     */
    fun formatMedicalInsurancePercent(): String {
        return formatNumber(_taxSettings.value.medicalInsuranceRate * 100)
    }

    /**
     * 格式化失业保险百分比为字符串（如 0.005 -> "0.5"）
     */
    fun formatUnemploymentInsurancePercent(): String {
        return formatNumber(_taxSettings.value.unemploymentInsuranceRate * 100)
    }

    /**
     * 格式化专项附加扣除为字符串
     */
    fun formatSpecialDeduction(): String {
        return formatNumber(_taxSettings.value.specialDeduction)
    }

    /**
     * 重置为默认设置
     */
    fun resetToDefaults() {
        val defaultSettings = TaxSettings()
        updateTaxSettings(defaultSettings)
    }
}
