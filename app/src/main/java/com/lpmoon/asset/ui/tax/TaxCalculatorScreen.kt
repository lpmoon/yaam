package com.lpmoon.asset.ui.tax

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxCalculatorScreen(
    onBack: () -> Unit
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
    var bonusAmount by remember { mutableStateOf("") }
    var monthlySalary by remember { mutableStateOf("") }
    var includeMonthlySalary by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    val bonusValue = bonusAmount.toDoubleOrNull() ?: 0.0
    val monthlySalaryValue = monthlySalary.toDoubleOrNull() ?: 0.0

    val result = calculateTax(bonusValue, if (includeMonthlySalary) monthlySalaryValue else 0.0)

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
    var monthlySalary by remember { mutableStateOf("") }
    var annualSalary by remember { mutableStateOf("") }
    var calculationMode by remember { mutableStateOf(0) } // 0: 月薪, 1: 年薪
    var socialSecurityRate by remember { mutableStateOf("0.08") } // 养老保险个人比例
    var housingFundRate by remember { mutableStateOf("0.12") } // 公积金个人比例
    var medicalInsuranceRate by remember { mutableStateOf("0.02") } // 医疗保险个人比例
    var unemploymentInsuranceRate by remember { mutableStateOf("0.005") } // 失业保险个人比例
    var specialDeduction by remember { mutableStateOf("0") } // 专项附加扣除
    var showResult by remember { mutableStateOf(false) }

    val monthlySalaryValue = monthlySalary.toDoubleOrNull() ?: 0.0
    val annualSalaryValue = annualSalary.toDoubleOrNull() ?: 0.0
    val socialSecurityRateValue = socialSecurityRate.toDoubleOrNull() ?: 0.08
    val housingFundRateValue = housingFundRate.toDoubleOrNull() ?: 0.12
    val medicalInsuranceRateValue = medicalInsuranceRate.toDoubleOrNull() ?: 0.02
    val unemploymentInsuranceRateValue = unemploymentInsuranceRate.toDoubleOrNull() ?: 0.005
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
            socialSecurityRate = socialSecurityRateValue,
            housingFundRate = housingFundRateValue,
            medicalInsuranceRate = medicalInsuranceRateValue,
            unemploymentInsuranceRate = unemploymentInsuranceRateValue,
            specialDeduction = specialDeductionValue
        )
    } else {
        null
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