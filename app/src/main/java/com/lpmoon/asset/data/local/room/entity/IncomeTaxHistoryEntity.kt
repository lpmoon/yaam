package com.lpmoon.asset.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lpmoon.asset.domain.model.tax.IncomeTaxHistory
import com.lpmoon.asset.domain.model.tax.IncomeTaxResult

/**
 * Room 普通收入计算器历史记录实体
 */
@Entity(tableName = "income_tax_histories")
data class IncomeTaxHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val calculationMode: Int,
    val inputSalary: Double,
    val socialSecurityRate: Double,
    val housingFundRate: Double,
    val medicalInsuranceRate: Double,
    val unemploymentInsuranceRate: Double,
    val specialDeduction: Double,
    // IncomeTaxResult 字段
    val monthlySalary: Double,
    val socialSecurity: Double,
    val housingFund: Double,
    val medicalInsurance: Double,
    val unemploymentInsurance: Double,
    val totalInsurance: Double,
    val taxableIncome: Double,
    val taxRate: Double,
    val quickDeduction: Double,
    val incomeTax: Double,
    val afterTaxMonthly: Double,
    val afterTaxAnnual: Double,
    val actualTaxRate: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): IncomeTaxHistory = IncomeTaxHistory(
        id = id,
        calculationMode = calculationMode,
        inputSalary = inputSalary,
        socialSecurityRate = socialSecurityRate,
        housingFundRate = housingFundRate,
        medicalInsuranceRate = medicalInsuranceRate,
        unemploymentInsuranceRate = unemploymentInsuranceRate,
        specialDeduction = specialDeduction,
        result = IncomeTaxResult(
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
            incomeTax = incomeTax,
            afterTaxMonthly = afterTaxMonthly,
            afterTaxAnnual = afterTaxAnnual,
            actualTaxRate = actualTaxRate
        ),
        timestamp = timestamp
    )

    companion object {
        fun fromDomainModel(history: IncomeTaxHistory): IncomeTaxHistoryEntity = IncomeTaxHistoryEntity(
            id = history.id,
            calculationMode = history.calculationMode,
            inputSalary = history.inputSalary,
            socialSecurityRate = history.socialSecurityRate,
            housingFundRate = history.housingFundRate,
            medicalInsuranceRate = history.medicalInsuranceRate,
            unemploymentInsuranceRate = history.unemploymentInsuranceRate,
            specialDeduction = history.specialDeduction,
            monthlySalary = history.result.monthlySalary,
            socialSecurity = history.result.socialSecurity,
            housingFund = history.result.housingFund,
            medicalInsurance = history.result.medicalInsurance,
            unemploymentInsurance = history.result.unemploymentInsurance,
            totalInsurance = history.result.totalInsurance,
            taxableIncome = history.result.taxableIncome,
            taxRate = history.result.taxRate,
            quickDeduction = history.result.quickDeduction,
            incomeTax = history.result.incomeTax,
            afterTaxMonthly = history.result.afterTaxMonthly,
            afterTaxAnnual = history.result.afterTaxAnnual,
            actualTaxRate = history.result.actualTaxRate,
            timestamp = history.timestamp
        )
    }
}
