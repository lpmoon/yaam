package com.lpmoon.asset.domain.model.tax

/**
 * 普通收入税率计算结果
 */
data class IncomeTaxResult(
    val monthlySalary: Double,           // 月薪
    val socialSecurity: Double,          // 养老保险
    val housingFund: Double,             // 公积金
    val medicalInsurance: Double,        // 医疗保险
    val unemploymentInsurance: Double,   // 失业保险
    val totalInsurance: Double,          // 五险一金合计
    val specialDeduction: Double,        // 专项附加扣除
    val taxableIncome: Double,           // 应纳税所得额
    val taxRate: Double,                 // 税率
    val quickDeduction: Double,          // 速算扣除数
    val incomeTax: Double,               // 应缴个税
    val afterTaxMonthly: Double,         // 税后月收入
    val afterTaxAnnual: Double,          // 税后年收入
    val actualTaxRate: Double            // 实际税率：个税/总收入
)