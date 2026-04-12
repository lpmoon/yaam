package com.lpmoon.asset.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lpmoon.asset.data.TaxSettings
import com.lpmoon.asset.viewmodel.TaxSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSettingsScreen(
    onBack: () -> Unit
) {
    val viewModel: TaxSettingsViewModel = viewModel()
    val taxSettings by viewModel.taxSettings.collectAsState()

    var socialSecurityRate by remember { mutableStateOf(viewModel.formatSocialSecurityPercent()) }
    var housingFundRate by remember { mutableStateOf(viewModel.formatHousingFundPercent()) }
    var medicalInsuranceRate by remember { mutableStateOf(viewModel.formatMedicalInsurancePercent()) }
    var unemploymentInsuranceRate by remember { mutableStateOf(viewModel.formatUnemploymentInsurancePercent()) }
    var specialDeduction by remember { mutableStateOf(viewModel.formatSpecialDeduction()) }

    // 当税率设置变化时，更新本地状态
    LaunchedEffect(taxSettings) {
        socialSecurityRate = viewModel.formatSocialSecurityPercent()
        housingFundRate = viewModel.formatHousingFundPercent()
        medicalInsuranceRate = viewModel.formatMedicalInsurancePercent()
        unemploymentInsuranceRate = viewModel.formatUnemploymentInsurancePercent()
        specialDeduction = viewModel.formatSpecialDeduction()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("税率设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 五险一金比例设置
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "五险一金比例",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingRow(
                        title = "养老保险",
                        value = socialSecurityRate,
                        suffix = "%",
                        onValueChange = { socialSecurityRate = it },
                        description = "个人缴纳比例，按8%设置"
                    )
                    SpacerSettingRow(title = "公积金", value = housingFundRate, suffix = "%", onValueChange = { housingFundRate = it }, description = "个人缴纳比例，按12%设置")
                    SpacerSettingRow(title = "医疗保险", value = medicalInsuranceRate, suffix = "%", onValueChange = { medicalInsuranceRate = it }, description = "个人缴纳比例，按2%设置")
                    SpacerSettingRow(title = "失业保险", value = unemploymentInsuranceRate, suffix = "%", onValueChange = { unemploymentInsuranceRate = it }, description = "个人缴纳比例，按0.5%设置")
                }
            }

            // 专项附加扣除设置
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "专项附加扣除",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingRow(
                        title = "每月扣除额",
                        value = specialDeduction,
                        suffix = "元",
                        onValueChange = { specialDeduction = it },
                        description = "子女教育、住房贷款利息等"
                    )
                }
            }

            // 保存按钮
            Button(
                onClick = {
                    viewModel.updateTaxSettings(
                        TaxSettings(
                            socialSecurityRate = (socialSecurityRate.toDoubleOrNull() ?: 8.0) / 100.0,
                            housingFundRate = (housingFundRate.toDoubleOrNull() ?: 12.0) / 100.0,
                            medicalInsuranceRate = (medicalInsuranceRate.toDoubleOrNull() ?: 2.0) / 100.0,
                            unemploymentInsuranceRate = (unemploymentInsuranceRate.toDoubleOrNull() ?: 0.5) / 100.0,
                            specialDeduction = specialDeduction.toDoubleOrNull() ?: 0.0
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存设置", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.0.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = suffix,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SpacerSettingRow(
    title: String,
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit,
    description: String
) {
    Spacer(modifier = Modifier.height(8.dp))
    SettingRow(title, value, suffix, onValueChange, description)
}
