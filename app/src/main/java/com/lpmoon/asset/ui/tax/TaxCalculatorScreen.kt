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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lpmoon.asset.domain.model.tax.BonusTaxResult
import com.lpmoon.asset.domain.model.tax.IncomeTaxResult
import com.lpmoon.asset.domain.usecase.tax.CalculateBonusTaxUseCase
import com.lpmoon.asset.domain.usecase.tax.CalculateIncomeTaxUseCase
import com.lpmoon.asset.presentation.viewmodel.TaxSettingsViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxCalculatorScreen(
    onBack: () -> Unit,
    onNavigateToConfiguration: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 年终奖计算, 1: 普通收入计算

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "年终奖税率计算器"
                            1 -> "普通收入税率计算器"
                            else -> "税率计算器"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                    label = { Text("税率计算") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToConfiguration,
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("配置") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 选项卡选择器
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("年终奖计算器")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("普通收入计算器")
                        }
                    }
                )
            }

            // 内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> TaxRateCalculatorContentWrapper()
                    1 -> IncomeTaxCalculatorContentWrapper()
                }
            }
        }
    }
}

@Composable
private fun TaxRateCalculatorContentWrapper() {
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
}

@Composable
private fun IncomeTaxCalculatorContentWrapper() {
    val viewModel: TaxSettingsViewModel = viewModel()
    val taxSettings by viewModel.taxSettings.collectAsState()
    val calculateIncomeTaxUseCase = remember { CalculateIncomeTaxUseCase() }

    var monthlySalary by remember { mutableStateOf("") }
    var annualSalary by remember { mutableStateOf("") }
    var calculationMode by remember { mutableStateOf(0) } // 0: 月薪, 1: 年薪
    var socialSecurityRate by remember { mutableStateOf(viewModel.formatSocialSecurityPercent()) } // 养老保险个人比例
    var housingFundRate by remember { mutableStateOf(viewModel.formatHousingFundPercent()) } // 公积金个人比例
    var medicalInsuranceRate by remember { mutableStateOf(viewModel.formatMedicalInsurancePercent()) } // 医疗保险个人比例
    var unemploymentInsuranceRate by remember { mutableStateOf(viewModel.formatUnemploymentInsurancePercent()) } // 失业保险个人比例
    var specialDeduction by remember { mutableStateOf(viewModel.formatSpecialDeduction()) } // 专项附加扣除
    var showResult by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<IncomeTaxResult?>(null) }

    // 当税率设置变化时，更新本地状态
    LaunchedEffect(taxSettings) {
        socialSecurityRate = viewModel.formatSocialSecurityPercent()
        housingFundRate = viewModel.formatHousingFundPercent()
        medicalInsuranceRate = viewModel.formatMedicalInsurancePercent()
        unemploymentInsuranceRate = viewModel.formatUnemploymentInsurancePercent()
        specialDeduction = viewModel.formatSpecialDeduction()
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

    LaunchedEffect(baseMonthlySalary, socialSecurityRateValue, housingFundRateValue, medicalInsuranceRateValue, unemploymentInsuranceRateValue, specialDeductionValue) {
        if (baseMonthlySalary > 0) {
            result = calculateIncomeTaxUseCase(
                CalculateIncomeTaxUseCase.Params(
                    monthlySalary = baseMonthlySalary,
                    socialSecurityRate = socialSecurityRateValue / 100.0,
                    housingFundRate = housingFundRateValue / 100.0,
                    medicalInsuranceRate = medicalInsuranceRateValue / 100.0,
                    unemploymentInsuranceRate = unemploymentInsuranceRateValue / 100.0,
                    specialDeduction = specialDeductionValue
                )
            )
        }
    }

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
}

@Composable
private fun TaxRateCalculatorContent(
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

                    androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant)

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

                    androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant)

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
private fun IncomeTaxCalculatorContent(
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

                    androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant)

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

                    androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant)

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

                    androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant)

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
