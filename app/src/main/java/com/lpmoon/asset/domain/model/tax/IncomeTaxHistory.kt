package com.lpmoon.asset.domain.model.tax

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 普通收入计算器历史记录领域模型
 */
data class IncomeTaxHistory(
    val id: Long = 0,
    val calculationMode: Int,        // 0: 月薪, 1: 年薪
    val inputSalary: Double,          // 用户输入的月薪或年薪
    val socialSecurityRate: Double,   // 养老保险比例（小数）
    val housingFundRate: Double,      // 公积金比例（小数）
    val medicalInsuranceRate: Double, // 医疗保险比例（小数）
    val unemploymentInsuranceRate: Double, // 失业保险比例（小数）
    val specialDeduction: Double,     // 专项附加扣除
    val result: IncomeTaxResult,      // 计算结果快照
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getSalaryDisplay(): String {
        return if (calculationMode == 0) {
            "月薪 ¥${"%,.2f".format(inputSalary)}"
        } else {
            "年薪 ¥${"%,.2f".format(inputSalary)}"
        }
    }
}
