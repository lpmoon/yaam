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
import com.lpmoon.asset.domain.model.tax.BonusTaxResult
import com.lpmoon.asset.domain.usecase.tax.CalculateBonusTaxUseCase
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxRateCalculatorScreen(
    onBack: () -> Unit,
    isEmbedded: Boolean = false
) {
    val calculateBonusTaxUseCase = remember { CalculateBonusTaxUseCase() }

    var bonusAmount by remember { mutableStateOf("") }
    var monthlySalary by remember { mutableStateOf("") }
    var includeMonthlySalary by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<BonusTaxResult?>(null) }

    val bonusValue = bonusAmount.toDoubleOrNull() ?: 0.0
    val monthlySalaryValue = monthlySalary.toDoubleOrNull() ?: 0.0

    LaunchedEffect(bonusValue, monthlySalaryValue, includeMonthlySalary) {
        if (bonusValue > 0) {
            result = calculateBonusTaxUseCase(
                CalculateBonusTaxUseCase.Params(
                    bonusAmount = bonusValue,
                    monthlySalary = if (includeMonthlySalary) monthlySalaryValue else 0.0
                )
            )
        }
    }

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
    result: BonusTaxResult?,
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
                    text = "计算年终奖个人所得税。如果当月工资低于5000元起征点，年终奖计税基数会相应调整。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 年终奖输入
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "年终奖金额",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = bonusAmount,
                    onValueChange = onBonusAmountChange,
                    label = { Text("年终奖（元）") },
                    placeholder = { Text("请输入年终奖金额") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
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
            }
        }

        // 当月工资选项
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "当月工资",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "如果当月工资低于5000元，会调整年终奖计税基数",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = includeMonthlySalary,
                        onCheckedChange = onIncludeMonthlySalaryChange
                    )
                }

                if (includeMonthlySalary) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = monthlySalary,
                        onValueChange = onMonthlySalaryChange,
                        label = { Text("当月工资（元）") },
                        placeholder = { Text("请输入当月工资") },
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = monthlySalary.isNotEmpty() && monthlySalaryValue < 0
                    )
                    if (monthlySalary.isNotEmpty() && monthlySalaryValue < 0) {
                        Text(
                            text = "请输入有效的金额",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 计算按钮
        Button(
            onClick = {
                if (bonusValue > 0) {
                    onShowResultChange(true)
                }
            },
            enabled = bonusValue > 0 && (!includeMonthlySalary || monthlySalaryValue >= 0),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("计算税费")
        }

        // 结果卡片
        if (showResult && result != null) {
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
                        label = "年终奖",
                        value = "¥${DecimalFormat("#,##0.00").format(result.bonus)}"
                    )

                    if (includeMonthlySalary) {
                        ResultItem(
                            label = "当月工资",
                            value = "¥${DecimalFormat("#,##0.00").format(result.monthlySalary)}"
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "税费计算",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "合计收入",
                        value = "¥${DecimalFormat("#,##0.00").format(result.total)}"
                    )

                    ResultItem(
                        label = "应纳税额",
                        value = "¥${DecimalFormat("#,##0.00").format(result.taxable)}"
                    )

                    ResultItem(
                        label = "适用税率",
                        value = "${(result.rate * 100).toInt()}%",
                        valueColor = MaterialTheme.colorScheme.error,
                        valueFontWeight = FontWeight.Bold
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

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    ResultItem(
                        label = "税后收入",
                        value = "¥${DecimalFormat("#,##0.00").format(result.afterTax)}",
                        valueColor = MaterialTheme.colorScheme.primary,
                        valueFontWeight = FontWeight.Bold
                    )

                    ResultItem(
                        label = "实际税率",
                        value = "${DecimalFormat("#,##0.00%").format(result.finalRate)}",
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
