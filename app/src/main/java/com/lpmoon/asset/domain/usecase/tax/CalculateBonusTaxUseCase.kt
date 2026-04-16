package com.lpmoon.asset.domain.usecase.tax

import com.lpmoon.asset.domain.model.tax.BonusTaxResult
import com.lpmoon.asset.domain.usecase.UseCase

/**
 * 年终奖税率计算用例
 */
class CalculateBonusTaxUseCase : UseCase<CalculateBonusTaxUseCase.Params, BonusTaxResult> {

    data class Params(
        val bonusAmount: Double,
        val monthlySalary: Double
    )

    override suspend operator fun invoke(params: Params): BonusTaxResult {
        val total = params.bonusAmount + params.monthlySalary

        // 个人所得税起征点
        val taxThreshold = 5000.0

        // 计算年终奖计税基数（考虑当月工资低于起征点的差额）
        var taxableBonus = params.bonusAmount
        if (params.monthlySalary > 0 && params.monthlySalary < taxThreshold) {
            val shortfall = taxThreshold - params.monthlySalary
            taxableBonus = maxOf(0.0, params.bonusAmount - shortfall)
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
            bonus = params.bonusAmount,
            monthlySalary = params.monthlySalary,
            total = total,
            rate = rate,
            taxable = taxableBonus,
            deduction = deduction,
            tax = finalTax,
            afterTax = afterTax,
            finalRate = finalRate
        )
    }
}
