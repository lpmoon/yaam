package com.lpmoon.asset.ui.tax

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lpmoon.asset.ui.config.TaxSettingsManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

// 格式化数字为字符串，去除不必要的精度
private fun formatNumber(value: Double): String {
    val decimal = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    return if (decimal.scale() < 0) {
        decimal.toBigInteger().toString()
    } else {
        decimal.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeTaxCalculatorScreen(
    onBack: () -> Unit,
    isEmbedded: Boolean = false
) {
    val taxSettings by TaxSettingsManager.taxSettings.collectAsState()

    var monthlySalary by remember { mutableStateOf("") }
    var annualSalary by remember { mutableStateOf("") }
    var calculationMode by remember { mutableStateOf(0) } // 0: 月薪, 1: 年薪
    var socialSecurityRate by remember { mutableStateOf(formatNumber(taxSettings.socialSecurityRate * 100)) } // 养老保险个人比例
    var housingFundRate by remember { mutableStateOf(formatNumber(taxSettings.housingFundRate * 100)) } // 公积金个人比例
    var medicalInsuranceRate by remember { mutableStateOf(formatNumber(taxSettings.medicalInsuranceRate * 100)) } // 医疗保险个人比例
    var unemploymentInsuranceRate by remember { mutableStateOf(formatNumber(taxSettings.unemploymentInsuranceRate * 100)) } // 失业保险个人比例
    var specialDeduction by remember { mutableStateOf(formatNumber(taxSettings.specialDeduction)) } // 专项附加扣除
    var showResult by remember { mutableStateOf(false) }

    // 当税率设置变化时，更新本地状态
    LaunchedEffect(taxSettings) {
        socialSecurityRate = formatNumber(taxSettings.socialSecurityRate * 100)
        housingFundRate = formatNumber(taxSettings.housingFundRate * 100)
        medicalInsuranceRate = formatNumber(taxSettings.medicalInsuranceRate * 100)
        unemploymentInsuranceRate = formatNumber(taxSettings.unemploymentInsuranceRate * 100)
        specialDeduction = formatNumber(taxSettings.specialDeduction)
    }

    val monthlySalaryValue = monthlySalary.toDoubleOrNull() ?: 0.0
    val annualSalaryValue = annualSalary.toDoubleOrNull() ?: 0.0
    val socialSecurityRateValue = socialSecurityRate.toDoubleOrNull() ?: 8.0
    val housingFundRateValue = housingFundRate.toDoubleOrNull() ?: 12.0
    val medicalInsuranceRateValue = medicalInsuranceRate.toDoubleOrNull() ?: 2.0
    val unemploymentInsuranceRateValue = unemploymentInsuranceRate.toDoubleOrNull() ?: 0.5
    val specialDeductionValue = specialDeduction.toDoubleOrNull() ?: 0.0

    // 根据计算模式确定月薪
    val baseMonthlySalary = when (calculationMode) {
        0 -> monthlySalaryValue
        1 -> if (annualSalaryValue > 0) annualSalaryValue / 12.0 else 0.0
        else -> 0.0
    }

    val result = if (baseMonthlySalary > 0) {
        calculateIncomeTax(
            monthlySalary = baseMonthlySalary,
            socialSecurityRate = socialSecurityRateValue / 100.0,
            housingFundRate = housingFundRateValue / 100.0,
            medicalInsuranceRate = medicalInsuranceRateValue / 100.0,
            unemploymentInsuranceRate = unemploymentInsuranceRateValue / 100.0,
            specialDeduction = specialDeductionValue
        )
    } else {
        null
    }

    if (isEmbedded) {
        // 在嵌入模式下，只显示内容部分
        IncomeTaxCalculatorContent(
            monthlySalary = monthlySalary,
            annualSalary = annualSalary,
            calculationMode = calculationMode,
            socialSecurityRate = socialSecurityRate,
            housingFundRate = housingFundRate,
            medicalInsuranceRate = medicalInsuranceRate,
            unemploymentInsuranceRate = unemploymentInsuranceRate,
            specialDeduction = specialDeduction,
            showResult = showResult,
            monthlySalaryValue = monthlySalaryValue,
            annualSalaryValue = annualSalaryValue,
            socialSecurityRateValue = socialSecurityRateValue,
            housingFundRateValue = housingFundRateValue,
            medicalInsuranceRateValue = medicalInsuranceRateValue,
            unemploymentInsuranceRateValue = unemploymentInsuranceRateValue,
            specialDeductionValue = specialDeductionValue,
            baseMonthlySalary = baseMonthlySalary,
            result = result,
            onMonthlySalaryChange = { monthlySalary = it },
            onAnnualSalaryChange = { annualSalary = it },
            onCalculationModeChange = { calculationMode = it },
            onSocialSecurityRateChange = { socialSecurityRate = it },
            onHousingFundRateChange = { housingFundRate = it },
            onMedicalInsuranceRateChange = { medicalInsuranceRate = it },
            onUnemploymentInsuranceRateChange = { unemploymentInsuranceRate = it },
            onSpecialDeductionChange = { specialDeduction = it },
            onShowResultChange = { showResult = it },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // 独立模式下，显示完整的Scaffold
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("普通收入税率计算器") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    tonalElevation = 4.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onBack,
                        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                        label = { Text("个人资产") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        label = { Text("年终奖计算器") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        label = { Text("普通收入计算器") }
                    )
                }
            }
        ) { paddingValues ->
            IncomeTaxCalculatorContent(
                monthlySalary = monthlySalary,
                annualSalary = annualSalary,
                calculationMode = calculationMode,
                socialSecurityRate = socialSecurityRate,
                housingFundRate = housingFundRate,
                medicalInsuranceRate = medicalInsuranceRate,
                unemploymentInsuranceRate = unemploymentInsuranceRate,
                specialDeduction = specialDeduction,
                showResult = showResult,
                monthlySalaryValue = monthlySalaryValue,
                annualSalaryValue = annualSalaryValue,
                socialSecurityRateValue = socialSecurityRateValue,
                housingFundRateValue = housingFundRateValue,
                medicalInsuranceRateValue = medicalInsuranceRateValue,
                unemploymentInsuranceRateValue = unemploymentInsuranceRateValue,
                specialDeductionValue = specialDeductionValue,
                baseMonthlySalary = baseMonthlySalary,
                result = result,
                onMonthlySalaryChange = { monthlySalary = it },
                onAnnualSalaryChange = { annualSalary = it },
                onCalculationModeChange = { calculationMode = it },
                onSocialSecurityRateChange = { socialSecurityRate = it },
                onHousingFundRateChange = { housingFundRate = it },
                onMedicalInsuranceRateChange = { medicalInsuranceRate = it },
                onUnemploymentInsuranceRateChange = { unemploymentInsuranceRate = it },
                onSpecialDeductionChange = { specialDeduction = it },
                onShowResultChange = { showResult = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
fun IncomeTaxCalculatorContent(
    monthlySalary: String,
    annualSalary: String,
    calculationMode: Int,
    socialSecurityRate: String,
    housingFundRate: String,
    medicalInsuranceRate: String,
    unemploymentInsuranceRate: String,
    specialDeduction: String,
    showResult: Boolean,
    monthlySalaryValue: Double,
    annualSalaryValue: Double,
    socialSecurityRateValue: Double,
    housingFundRateValue: Double,
    medicalInsuranceRateValue: Double,
    unemploymentInsuranceRateValue: Double,
    specialDeductionValue: Double,
    baseMonthlySalary: Double,
    result: IncomeTaxResult?,
    onMonthlySalaryChange: (String) -> Unit,
    onAnnualSalaryChange: (String) -> Unit,
    onCalculationModeChange: (Int) -> Unit,
    onSocialSecurityRateChange: (String) -> Unit,
    onHousingFundRateChange: (String) -> Unit,
    onMedicalInsuranceRateChange: (String) -> Unit,
    onUnemploymentInsuranceRateChange: (String) -> Unit,
    onSpecialDeductionChange: (String) -> Unit,
    onShowResultChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 说明卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "使用说明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "计算普通收入个人所得税，可选择按月薪或年薪计算。系统默认五险一金比例，可根据实际情况调整。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 计算模式选择
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "计算模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterChip(
                        selected = calculationMode == 0,
                        onClick = { onCalculationModeChange(0) },
                        label = { Text("按月薪计算") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = calculationMode == 1,
                        onClick = { onCalculationModeChange(1) },
                        label = { Text("按年薪计算") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 月薪输入
                if (calculationMode == 0) {
                    OutlinedTextField(
                        value = monthlySalary,
                        onValueChange = onMonthlySalaryChange,
                        label = { Text("月薪金额（元）") },
                        placeholder = { Text("请输入月薪金额") },
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = monthlySalary.isNotEmpty() && monthlySalaryValue <= 0
                    )
                    if (monthlySalary.isNotEmpty() && monthlySalaryValue <= 0) {
                        Text(
                            text = "请输入有效的金额",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    // 年薪输入
                    OutlinedTextField(
                        value = annualSalary,
                        onValueChange = onAnnualSalaryChange,
                        label = { Text("年薪金额（元）") },
                        placeholder = { Text("请输入年薪金额") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = annualSalary.isNotEmpty() && annualSalaryValue <= 0
                    )
                    if (annualSalary.isNotEmpty() && annualSalaryValue <= 0) {
                        Text(
                            text = "请输入有效的金额",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 五险一金比例设置
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "五险一金比例设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // 养老保险比例
                OutlinedTextField(
                    value = socialSecurityRate,
                    onValueChange = onSocialSecurityRateChange,
                    label = { Text("养老保险个人比例") },
                    placeholder = { Text("例如：8 表示 8%") },
                    trailingIcon = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = socialSecurityRate.isNotEmpty() && (socialSecurityRateValue < 0 || socialSecurityRateValue > 100)
                )

                // 公积金比例
                OutlinedTextField(
                    value = housingFundRate,
                    onValueChange = onHousingFundRateChange,
                    label = { Text("公积金个人比例") },
                    placeholder = { Text("例如：12 表示 12%") },
                    trailingIcon = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = housingFundRate.isNotEmpty() && (housingFundRateValue < 0 || housingFundRateValue > 100)
                )

                // 医疗保险比例
                OutlinedTextField(
                    value = medicalInsuranceRate,
                    onValueChange = onMedicalInsuranceRateChange,
                    label = { Text("医疗保险个人比例") },
                    placeholder = { Text("例如：2 表示 2%") },
                    trailingIcon = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = medicalInsuranceRate.isNotEmpty() && (medicalInsuranceRateValue < 0 || medicalInsuranceRateValue > 100)
                )

                // 失业保险比例
                OutlinedTextField(
                    value = unemploymentInsuranceRate,
                    onValueChange = onUnemploymentInsuranceRateChange,
                    label = { Text("失业保险个人比例") },
                    placeholder = { Text("例如：0.5 表示 0.5%") },
                    trailingIcon = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = unemploymentInsuranceRate.isNotEmpty() && (unemploymentInsuranceRateValue < 0 || unemploymentInsuranceRateValue > 100)
                )
            }
        }

        // 专项附加扣除
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "专项附加扣除（每月）",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = specialDeduction,
                    onValueChange = onSpecialDeductionChange,
                    label = { Text("专项附加扣除金额（元）") },
                    placeholder = { Text("例如：1000（子女教育、房贷利息等）") },
                    leadingIcon = {
                        Icon(Icons.Default.Savings, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = specialDeduction.isNotEmpty() && specialDeductionValue < 0
                )
                if (specialDeduction.isNotEmpty() && specialDeductionValue < 0) {
                    Text(
                        text = "请输入有效的金额",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 计算按钮
        Button(
            onClick = {
                if (baseMonthlySalary > 0) {
                    onShowResultChange(true)
                }
            },
            enabled = baseMonthlySalary > 0 &&
                    socialSecurityRateValue >= 0 && socialSecurityRateValue <= 100 &&
                    housingFundRateValue >= 0 && housingFundRateValue <= 100 &&
                    medicalInsuranceRateValue >= 0 && medicalInsuranceRateValue <= 100 &&
                    unemploymentInsuranceRateValue >= 0 && unemploymentInsuranceRateValue <= 100 &&
                    specialDeductionValue >= 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("计算税费")
        }

        // 结果卡片
        if (showResult && result != null && baseMonthlySalary > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "计算结果",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    ResultItem(
                        label = if (calculationMode == 0) "月薪" else "年薪",
                        value = "¥${DecimalFormat("#,##0.00").format(if (calculationMode == 0) monthlySalaryValue else annualSalaryValue)}"
                    )

                    if (calculationMode == 1) {
                        ResultItem(
                            label = "月均收入",
                            value = "¥${DecimalFormat("#,##0.00").format(baseMonthlySalary)}"
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "五险一金扣除",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "养老保险",
                        value = "¥${DecimalFormat("#,##0.00").format(result.socialSecurity)}"
                    )

                    ResultItem(
                        label = "公积金",
                        value = "¥${DecimalFormat("#,##0.00").format(result.housingFund)}"
                    )

                    ResultItem(
                        label = "医疗保险",
                        value = "¥${DecimalFormat("#,##0.00").format(result.medicalInsurance)}"
                    )

                    ResultItem(
                        label = "失业保险",
                        value = "¥${DecimalFormat("#,##0.00").format(result.unemploymentInsurance)}"
                    )

                    ResultItem(
                        label = "五险一金合计",
                        value = "¥${DecimalFormat("#,##0.00").format(result.totalInsurance)}",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "专项附加扣除",
                        value = "¥${DecimalFormat("#,##0.00").format(result.specialDeduction)}",
                        valueColor = MaterialTheme.colorScheme.primary,
                        valueFontWeight = FontWeight.Normal
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "个人所得税计算",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "应纳税所得额",
                        value = "¥${DecimalFormat("#,##0.00").format(result.taxableIncome)}"
                    )

                    ResultItem(
                        label = "适用税率",
                        value = "${(result.taxRate * 100).toInt()}%",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "速算扣除数",
                        value = "¥${DecimalFormat("#,##0.00").format(result.quickDeduction)}"
                    )

                    ResultItem(
                        label = "应缴个税",
                        value = "¥${DecimalFormat("#,##0.00").format(result.incomeTax)}",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    ResultItem(
                        label = "税后月收入",
                        value = "¥${DecimalFormat("#,##0.00").format(result.afterTaxMonthly)}",
                        valueColor = MaterialTheme.colorScheme.primary,
                        valueFontWeight = FontWeight.Bold
                    )

                    if (calculationMode == 1) {
                        ResultItem(
                            label = "税后年收入",
                            value = "¥${DecimalFormat("#,##0.00").format(result.afterTaxAnnual)}",
                            valueColor = MaterialTheme.colorScheme.primary,
                            valueFontWeight = FontWeight.Bold
                        )
                    }

                    ResultItem(
                        label = "实际税率",
                        value = "${DecimalFormat("#,##0.00%").format(result.actualTaxRate)}",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    valueFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = valueFontWeight
        )
    }
}

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
 * 计算普通收入个人所得税
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