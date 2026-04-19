package com.lpmoon.asset.domain.model.tax

/**
 * 税率设置配置
 */
data class TaxSettings(
    // 五险一金比例
    val socialSecurityRate: Double = 0.08,      // 养老保险个人比例
    val housingFundRate: Double = 0.12,         // 公积金个人比例
    val medicalInsuranceRate: Double = 0.02,    // 医疗保险个人比例
    val unemploymentInsuranceRate: Double = 0.005, // 失业保险个人比例
    // 专项附加扣除
    val specialDeduction: Double = 0.0          // 专项附加扣除（元/月）
)