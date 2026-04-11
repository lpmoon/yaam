package com.lpmoon.asset.ui.asset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lpmoon.asset.data.AssetType
import com.lpmoon.asset.data.CurrencyType
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDialog(
    initialName: String = "",
    initialValue: String = "",
    initialCurrency: String = CurrencyType.CNY.name,
    initialType: String = AssetType.OTHER.name,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var value by remember { mutableStateOf(initialValue) }
    var selectedCurrency by remember { mutableStateOf(CurrencyType.fromString(initialCurrency)) }
    var selectedType by remember { mutableStateOf(AssetType.fromString(initialType)) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var bankExpanded by remember { mutableStateOf(false) }

    // 银行列表
    val banks = listOf("中国银行", "招商银行", "汇丰银行", "其他（手动输入）")
    var selectedBank by remember { mutableStateOf(if (banks.contains(initialName)) initialName else banks[banks.size - 1]) }

    // 自动填充微信/支付宝名称
    LaunchedEffect(selectedType) {
        when (selectedType) {
            AssetType.WECHAT -> name = "微信"
            AssetType.ALIPAY -> name = "支付宝"
            AssetType.BANK_DEPOSIT -> {
                if (selectedBank != banks[banks.size - 1]) {
                    name = selectedBank
                }
            }
            else -> {
                // 其他类型不清空名称，保持用户输入
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (initialName.isEmpty()) "添加资产" else "编辑资产",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedType) {
                    AssetType.WECHAT, AssetType.ALIPAY -> {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {},
                            label = { Text("资产名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true,
                            enabled = false
                        )
                    }
                    AssetType.BANK_DEPOSIT -> {
                        ExposedDropdownMenuBox(
                            expanded = bankExpanded,
                            onExpandedChange = { bankExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedBank,
                                onValueChange = { newBank ->
                                    selectedBank = newBank
                                    if (newBank != banks[banks.size - 1]) {
                                        name = newBank
                                    } else {
                                        name = ""
                                    }
                                },
                                label = { Text("选择银行") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = bankExpanded,
                                onDismissRequest = { bankExpanded = false }
                            ) {
                                banks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = { Text(bank) },
                                        onClick = {
                                            selectedBank = bank
                                            if (bank != banks[banks.size - 1]) {
                                                name = bank
                                            } else {
                                                name = ""
                                            }
                                            bankExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("银行名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    else -> {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("资产名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("资产金额/表达式") },
                    placeholder = { Text("例如：100 或 100+50") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCurrency.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("货币类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        CurrencyType.entries.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency.displayName) },
                                onClick = {
                                    selectedCurrency = currency
                                    currencyExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("资产类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        AssetType.entries.forEach { assetType ->
                            DropdownMenuItem(
                                text = { Text(assetType.displayName) },
                                onClick = {
                                    selectedType = assetType
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(name, value, selectedCurrency.name, selectedType.name)
                        },
                        enabled = name.isNotBlank() && value.isNotBlank()
                    ) {
                        Text(if (initialName.isEmpty()) "添加" else "保存")
                    }
                }
            }
        }
    }
}