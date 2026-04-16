package com.lpmoon.asset.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lpmoon.asset.data.repository.TaxSettingsRepositoryImpl
import com.lpmoon.asset.domain.model.tax.TaxSettings
import com.lpmoon.asset.domain.usecase.tax.LoadTaxSettingsUseCase
import com.lpmoon.asset.domain.usecase.tax.SaveTaxSettingsUseCase
import com.lpmoon.asset.util.formatNumber
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 税率设置 ViewModel
 * 管理税率设置的状态、百分比转换逻辑
 */
class TaxSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaxSettingsRepositoryImpl(application)
    private val loadTaxSettingsUseCase = LoadTaxSettingsUseCase(repository)
    private val saveTaxSettingsUseCase = SaveTaxSettingsUseCase(repository)

    val taxSettings: StateFlow<TaxSettings> = repository.getTaxSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaxSettings()
        )

    init {
        // 在初始化时加载设置
        loadSettings()
    }

    /**
     * 加载税率设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            loadTaxSettingsUseCase()
        }
    }

    /**
     * 更新所有税率设置
     */
    fun updateTaxSettings(settings: TaxSettings) {
        viewModelScope.launch {
            saveTaxSettingsUseCase(settings)
        }
    }

    /**
     * 更新养老保险比例（传入百分比，如 8 表示 8%）
     */
    fun updateSocialSecuritySecurityPercent(percent: Double) {
        val rate = percent / 100.0
        updateSocialSecurityRate(rate)
    }

    /**
     * 更新养老保险比例（传入小数，如 0.08 表示 8%）
     */
    fun updateSocialSecurityRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = taxSettings.value.copy(socialSecurityRate = rate)
            saveTaxSettingsUseCase(newSettings)
        }
    }

    /**
     * 更新公积金比例（传入百分比，如 12 表示 12%）
     */
    fun updateHousingFundPercent(percent: Double) {
        val rate = percent / 100.0
        updateHousingFundRate(rate)
    }

    /**
     * 更新公积金比例（传入小数，如 0.12 表示 12%）
     */
    fun updateHousingFundRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = taxSettings.value.copy(housingFundRate = rate)
            saveTaxSettingsUseCase(newSettings)
        }
    }

    /**
     * 更新医疗保险比例（传入百分比，如 2 表示 2%）
     */
    fun updateMedicalInsurancePercent(percent: Double) {
        val rate = percent / 100.0
        updateMedicalInsuranceRate(rate)
    }

    /**
     * 更新医疗保险比例（传入小数，如 0.02 表示 2%）
     */
    fun updateMedicalInsuranceRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = taxSettings.value.copy(medicalInsuranceRate = rate)
            saveTaxSettingsUseCase(newSettings)
        }
    }

    /**
     * 更新失业保险比例（传入百分比，如 0.5 表示 0.5%）
     */
    fun updateUnemploymentInsurancePercent(percent: Double) {
        val rate = percent / 100.0
        updateUnemploymentInsuranceRate(rate)
    }

    /**
     * 更新失业保险比例（传入小数，如 0.005 表示 0.5%）
     */
    fun updateUnemploymentInsuranceRate(rate: Double) {
        viewModelScope.launch {
            val newSettings = taxSettings.value.copy(unemploymentInsuranceRate = rate)
            saveTaxSettingsUseCase(newSettings)
        }
    }

    /**
     * 更新专项附加扣除
     */
    fun updateSpecialDeduction(amount: Double) {
        viewModelScope.launch {
            val newSettings = taxSettings.value.copy(specialDeduction = amount)
            saveTaxSettingsUseCase(newSettings)
        }
    }

    /**
     * 格式化养老保险百分比为字符串（如 0.08 -> "8"）
     */
    fun formatSocialSecurityPercent(): String {
        return formatNumber(taxSettings.value.socialSecurityRate * 100)
    }

    /**
     * 格式化公积金百分比为字符串（如 0.12 -> "12"）
     */
    fun formatHousingFundPercent(): String {
        return formatNumber(taxSettings.value.housingFundRate * 100)
    }

    /**
     * 格式化医疗保险百分比为字符串（如 0.02 -> "2"）
     */
    fun formatMedicalInsurancePercent(): String {
        return formatNumber(taxSettings.value.medicalInsuranceRate * 100)
    }

    /**
     * 格式化失业保险百分比为字符串（如 0.005 -> "0.5"）
     */
    fun formatUnemploymentInsurancePercent(): String {
        return formatNumber(taxSettings.value.unemploymentInsuranceRate * 100)
    }

    /**
     * 格式化专项附加扣除为字符串
     */
    fun formatSpecialDeduction(): String {
        return formatNumber(taxSettings.value.specialDeduction)
    }

    /**
     * 重置为默认设置
     */
    fun resetToDefaults() {
        val defaultSettings = TaxSettings()
        updateTaxSettings(defaultSettings)
    }
}
