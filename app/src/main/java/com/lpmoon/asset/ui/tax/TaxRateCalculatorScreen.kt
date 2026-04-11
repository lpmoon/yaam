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
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxRateCalculatorScreen(
    onBack: () -> Unit,
    isEmbedded: Boolean = false
) {
    var bonusAmount by remember { mutableStateOf("") }
    var monthlySalary by remember { mutableStateOf("") }
    var includeMonthlySalary by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    val bonusValue = bonusAmount.toDoubleOrNull() ?: 0.0
    val monthlySalaryValue = monthlySalary.toDoubleOrNull() ?: 0.0

    val result = calculateTax(bonusValue, if (includeMonthlySalary) monthlySalaryValue else 0.0)

    if (isEmbedded) {
        // 在嵌入模式下，只显示内容部分
        TaxRateCalculatorContent(
            bonusAmount = bonusAmount,
            monthlySalary = monthlySalary,
            includeMonthlySalary = includeMonthlySalary,
            showResult = showResult,
            bonusValue = bonusValue,
            monthlySalaryValue = monthlySalaryValue,
            result = result,
            onBonusAmountChange = { bonusAmount = it },
            onMonthlySalaryChange = { monthlySalary = it },
            onIncludeMonthlySalaryChange = { includeMonthlySalary = it },
            onShowResultChange = { showResult = it },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // 独立模式下，显示完整的Scaffold
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("税率计算器") },
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
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                        label = { Text("税率计算器") }
                    )
                }
            }
        ) { paddingValues ->
            TaxRateCalculatorContent(
                bonusAmount = bonusAmount,
                monthlySalary = monthlySalary,
                includeMonthlySalary = includeMonthlySalary,
                showResult = showResult,
                bonusValue = bonusValue,
                monthlySalaryValue = monthlySalaryValue,
                result = result,
                onBonusAmountChange = { bonusAmount = it },
                onMonthlySalaryChange = { monthlySalary = it },
                onIncludeMonthlySalaryChange = { includeMonthlySalary = it },
                onShowResultChange = { showResult = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
fun TaxRateCalculatorContent(
    bonusAmount: String,
    monthlySalary: String,
    includeMonthlySalary: Boolean,
    showResult: Boolean,
    bonusValue: Double,
    monthlySalaryValue: Double,
    result: TaxResult,
    onBonusAmountChange: (String) -> Unit,
    onMonthlySalaryChange: (String) -> Unit,
    onIncludeMonthlySalaryChange: (Boolean) -> Unit,
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
                    text = "根据中国个人所得税法，年终奖采用单独计税方式：年终奖除以12个月确定税率，当月工资低于5000元时，可从年终奖中扣除差额。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 输入卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 年终奖金额
                OutlinedTextField(
                    value = bonusAmount,
                    onValueChange = onBonusAmountChange,
                    label = { Text("年终奖金额（元）") },
                    placeholder = { Text("请输入年终奖金额") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = bonusAmount.isNotEmpty() && bonusValue <= 0
                )
                if (bonusAmount.isNotEmpty() && bonusValue <= 0) {
                    Text(
                        text = "请输入有效的金额",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 当月工资（可选）
                OutlinedTextField(
                    value = monthlySalary,
                    onValueChange = onMonthlySalaryChange,
                    label = { Text("当月工资（元）") },
                    placeholder = { Text("请输入当月工资，不包含年终奖") },
                    leadingIcon = {
                        Icon(Icons.Default.Payments, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = includeMonthlySalary,
                    isError = includeMonthlySalary && monthlySalary.isNotEmpty() && monthlySalaryValue < 0
                )
                if (includeMonthlySalary && monthlySalary.isNotEmpty() && monthlySalaryValue < 0) {
                    Text(
                        text = "请输入有效的金额",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 是否包含当月工资
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeMonthlySalary,
                        onCheckedChange = {
                            onIncludeMonthlySalaryChange(it)
                            if (!it) onMonthlySalaryChange("")
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("将当月工资并入计税")
                }

                // 计算按钮
                Button(
                    onClick = {
                        onShowResultChange(true)
                    },
                    enabled = bonusAmount.isNotEmpty() && bonusValue > 0 &&
                            (!includeMonthlySalary || (monthlySalary.isNotEmpty() && monthlySalaryValue >= 0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("计算税费")
                }
            }
        }

        // 结果卡片
        if (showResult && bonusValue > 0) {
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
                        label = "年终奖金额",
                        value = "¥${DecimalFormat("#,##0.00").format(result.bonus)}"
                    )

                    if (includeMonthlySalary) {
                        ResultItem(
                            label = "当月工资",
                            value = "¥${DecimalFormat("#,##0.00").format(result.monthlySalary)}"
                        )
                        ResultItem(
                            label = "合计收入",
                            value = "¥${DecimalFormat("#,##0.00").format(result.total)}"
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    ResultItem(
                        label = "适用税率",
                        value = "${(result.rate * 100).toInt()}%",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "应纳税额",
                        value = "¥${DecimalFormat("#,##0.00").format(result.taxable)}"
                    )

                    ResultItem(
                        label = "速算扣除数",
                        value = "¥${DecimalFormat("#,##0.00").format(result.deduction)}"
                    )

                    ResultItem(
                        label = "应缴个税",
                        value = "¥${DecimalFormat("#,##0.00").format(result.tax)}",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "最终税率",
                        value = "${DecimalFormat("#,##0.00%").format(result.finalRate)}",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    ResultItem(
                        label = "税后收入",
                        value = "¥${DecimalFormat("#,##0.00").format(result.afterTax)}",
                        valueColor = MaterialTheme.colorScheme.primary,
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
 * 税率计算结果
 */
data class TaxResult(
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
 * 计算税率和应缴个税
 */
fun calculateTax(bonusAmount: Double, monthlySalary: Double): TaxResult {
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

    return TaxResult(
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