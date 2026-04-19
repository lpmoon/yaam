package com.lpmoon.asset.domain.model.tax

/**
 * 年终奖税率计算结果
 */
data class BonusTaxResult(
    val bonus: Double,           // 年终奖金额
    val monthlySalary: Double,   // 当月工资
    val total: Double,           // 合计收入
    val rate: Double,            // 税率
    val taxable: Double,         // 应纳税额
    val deduction: Double,       // 速算扣除数
    val tax: Double,             // 应缴个税
    val afterTax: Double,        // 税后收入
    val finalRate: Double        // 最终税率：交的税/总金额
)