package com.lpmoon.asset.domain.repository.tax

import com.lpmoon.asset.domain.model.tax.TaxSettings
import kotlinx.coroutines.flow.Flow

/**
 * 税率设置仓库接口
 */
interface TaxSettingsRepository {
    fun getTaxSettings(): Flow<TaxSettings>
    suspend fun loadSettings(): TaxSettings
    suspend fun saveSettings(settings: TaxSettings)
    suspend fun clearSettings()
}