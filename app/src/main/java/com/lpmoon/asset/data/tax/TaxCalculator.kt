package com.lpmoon.asset.data.tax

import java.math.BigDecimal
import java.math.RoundingMode

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

/**
 * 格式化数字为字符串，去除不必要的精度
 */
fun formatNumber(value: Double): String {
    val decimal = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    return if (decimal.scale() < 0) {
        decimal.toBigInteger().toString()
    } else {
        decimal.toString()
    }
}

/**
 * 年终奖税率计算
 * @param bonusAmount 年终奖金额
 * @param monthlySalary 当月工资（用于计算当月工资低于起征点时的差额）
 */
fun calculateBonusTax(bonusAmount: Double, monthlySalary: Double): BonusTaxResult {
    val total = bonusAmount + monthlySalary

    // 个人所得税起征点
    val taxThreshold = 5000.0

    // 计算年终奖计税基数（考虑当月工资低于起征点的差额）
    var taxableBonus = bonusAmount
    if (monthlySalary > 0 && monthlySalary < taxThreshold) {
        val shortfall = taxThreshold - monthlySalary
        taxableBonus = maxOf(0.0, bonusAmount - shortfall)
    }

    // 年终奖税率表（根据年终奖除以12后的金额确定税率）
    val monthlyBonus = taxableBonus / 12.0
    val (rate, deduction) = when {
        monthlyBonus <= 0 -> Pair(0.0, 0.0)
        monthlyBonus <= 3000 -> Pair(0.03, 0.0)
        monthlyBonus <= 12000 -> Pair(0.10, 210.0)
        monthlyBonus <= 25000 -> Pair(0.20, 1410.0)
        monthlyBonus <= 35000 -> Pair(0.25, 2660.0)
        monthlyBonus <= 55000 -> Pair(0.30, 4410.0)
        monthlyBonus <= 80000 -> Pair(0.35, 7160.0)
        else -> Pair(0.45, 15160.0)
    }

    val tax = taxableBonus * rate - deduction
    val finalTax = maxOf(0.0, tax)
    val afterTax = total - finalTax
    val finalRate = if (total > 0) finalTax / total else 0.0

    return BonusTaxResult(
        bonus = bonusAmount,
        monthlySalary = monthlySalary,
        total = total,
        rate = rate,
        taxable = taxableBonus,
        deduction = deduction,
        tax = finalTax,
        afterTax = afterTax,
        finalRate = finalRate
    )
}

/**
 * 普通收入税率计算
 * @param monthlySalary 月薪
 * @param socialSecurityRate 养老保险个人比例（如 0.08 表示 8%）
 * @param housingFundRate 公积金个人比例（如 0.12 表示 12%）
 * @param medicalInsuranceRate 医疗保险个人比例（如 0.02 表示 2%）
 * @param unemploymentInsuranceRate 失业保险个人比例（如 0.005 表示 0.5%）
 * @param specialDeduction 专项附加扣除（每月，单位：元）
 */
fun calculateIncomeTax(
    monthlySalary: Double,
    socialSecurityRate: Double = 0.08,
    housingFundRate: Double = 0.12,
    medicalInsuranceRate: Double = 0.02,
    unemploymentInsuranceRate: Double = 0.005,
    specialDeduction: Double = 0.0
): IncomeTaxResult {
    // 五险一金计算（基于月薪）
    val socialSecurity = monthlySalary * socialSecurityRate
    val housingFund = monthlySalary * housingFundRate
    val medicalInsurance = monthlySalary * medicalInsuranceRate
    val unemploymentInsurance = monthlySalary * unemploymentInsuranceRate
    val totalInsurance = socialSecurity + housingFund + medicalInsurance + unemploymentInsurance

    // 个税起征点
    val taxThreshold = 5000.0

    // 应纳税所得额 = 月薪 - 五险一金 - 起征点 - 专项附加扣除
    val taxableIncome = maxOf(0.0, monthlySalary - totalInsurance - taxThreshold - specialDeduction)

    // 根据应纳税所得额确定税率（月度税率表）
    val (taxRate, quickDeduction) = when {
        taxableIncome <= 0 -> Pair(0.0, 0.0)
        taxableIncome <= 3000 -> Pair(0.03, 0.0)
        taxableIncome <= 12000 -> Pair(0.10, 210.0)
        taxableIncome <= 25000 -> Pair(0.20, 1410.0)
        taxableIncome <= 35000 -> Pair(0.25, 2660.0)
        taxableIncome <= 55000 -> Pair(0.30, 4410.0)
        taxableIncome <= 80000 -> Pair(0.35, 7160.0)
        else -> Pair(0.45, 15160.0)
    }

    // 计算个税
    val incomeTax = taxableIncome * taxRate - quickDeduction

    // 税后月收入
    val afterTaxMonthly = monthlySalary - totalInsurance - incomeTax

    // 税后年收入
    val afterTaxAnnual = afterTaxMonthly * 12

    // 实际税率 = 个税 / 月薪
    val actualTaxRate = if (monthlySalary > 0) incomeTax / monthlySalary else 0.0

    return IncomeTaxResult(
        monthlySalary = monthlySalary,
        socialSecurity = socialSecurity,
        housingFund = housingFund,
        medicalInsurance = medicalInsurance,
        unemploymentInsurance = unemploymentInsurance,
        totalInsurance = totalInsurance,
        specialDeduction = specialDeduction,
        taxableIncome = taxableIncome,
        taxRate = taxRate,
        quickDeduction = quickDeduction,
        incomeTax = maxOf(0.0, incomeTax),
        afterTaxMonthly = afterTaxMonthly,
        afterTaxAnnual = afterTaxAnnual,
        actualTaxRate = actualTaxRate
    )
}
