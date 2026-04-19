package com.lpmoon.asset.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lpmoon.asset.domain.model.tax.TaxSettings

/**
 * Room 税率设置实体（只保存单条最新记录，id 固定为 1）
 */
@Entity(tableName = "tax_settings")
data class TaxSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val socialSecurityRate: Double = 0.08,      // 养老保险个人比例
    val housingFundRate: Double = 0.12,         // 公积金个人比例
    val medicalInsuranceRate: Double = 0.02,    // 医疗保险个人比例
    val unemploymentInsuranceRate: Double = 0.005, // 失业保险个人比例
    val specialDeduction: Double = 0.0          // 专项附加扣除（元/月）
) {
    fun toDomainModel(): TaxSettings = TaxSettings(
        socialSecurityRate = socialSecurityRate,
        housingFundRate = housingFundRate,
        medicalInsuranceRate = medicalInsuranceRate,
        unemploymentInsuranceRate = unemploymentInsuranceRate,
        specialDeduction = specialDeduction
    )

    companion object {
        fun fromDomainModel(settings: TaxSettings): TaxSettingsEntity = TaxSettingsEntity(
            socialSecurityRate = settings.socialSecurityRate,
            housingFundRate = settings.housingFundRate,
            medicalInsuranceRate = settings.medicalInsuranceRate,
            unemploymentInsuranceRate = settings.unemploymentInsuranceRate,
            specialDeduction = settings.specialDeduction
        )
    }
}
