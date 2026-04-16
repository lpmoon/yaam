package com.lpmoon.asset.ui.tax

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lpmoon.asset.domain.model.tax.BonusTaxResult
import com.lpmoon.asset.domain.model.tax.IncomeTaxResult
import com.lpmoon.asset.domain.usecase.tax.CalculateBonusTaxUseCase
import com.lpmoon.asset.domain.usecase.tax.CalculateIncomeTaxUseCase
import com.lpmoon.asset.presentation.viewmodel.TaxSettingsViewModel

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
    val scope = rememberCoroutineScope()

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
