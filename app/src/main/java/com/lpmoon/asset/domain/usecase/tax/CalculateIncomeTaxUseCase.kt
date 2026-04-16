package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.model.tax.IncomeTaxResult
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 普通收入税率计算用例
 */
class CalculateIncomeTaxUseCase : UseCase<CalculateIncomeTaxUseCase.Params, IncomeTaxResult> {

    data class Params(
        val monthlySalary: Double,
        val socialSecurityRate: Double = 0.08,
        val housingFundRate: Double = 0.12,
        val medicalInsuranceRate: Double = 0.02,
        val unemploymentInsuranceRate: Double = 0.005,
        val specialDeduction: Double = 0.0
    )

    override suspend operator fun invoke(params: Params): IncomeTaxResult {
        // 五险一金计算（基于月薪）
        val socialSecurity = params.monthlySalary * params.socialSecurityRate
        val housingFund = params.monthlySalary * params.housingFundRate
        val medicalInsurance = params.monthlySalary * params.medicalInsuranceRate
        val unemploymentInsurance = params.monthlySalary * params.unemploymentInsuranceRate
        val totalInsurance = socialSecurity + housingFund + medicalInsurance + unemploymentInsurance

        // 个税起征点
        val taxThreshold = 5000.0

        // 应纳税所得额 = 月薪 - 五险一金 - 起征点 - 专项附加扣除
        val taxableIncome = maxOf(0.0, params.monthlySalary - totalInsurance - taxThreshold - params.specialDeduction)

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
        val afterTaxMonthly = params.monthlySalary - totalInsurance - incomeTax

        // 税后年收入
        val afterTaxAnnual = afterTaxMonthly * 12

        // 实际税率 = 个税 / 月薪
        val actualTaxRate = if (params.monthlySalary > 0) incomeTax / params.monthlySalary else 0.0

        return IncomeTaxResult(
            monthlySalary = params.monthlySalary,
            socialSecurity = socialSecurity,
            housingFund = housingFund,
            medicalInsurance = medicalInsurance,
            unemploymentInsurance = unemploymentInsurance,
            totalInsurance = totalInsurance,
            specialDeduction = params.specialDeduction,
            taxableIncome = taxableIncome,
            taxRate = taxRate,
            quickDeduction = quickDeduction,
            incomeTax = maxOf(0.0, incomeTax),
            afterTaxMonthly = afterTaxMonthly,
            afterTaxAnnual = afterTaxAnnual,
            actualTaxRate = actualTaxRate
        )
    }
}
