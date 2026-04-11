package com.lpmoon.asset.ui.asset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import com.lpmoon.asset.data.Asset
import com.lpmoon.asset.data.AssetHistory
import com.lpmoon.asset.data.AssetType
import com.lpmoon.asset.data.CurrencyType
import com.lpmoon.asset.data.ExchangeRate
import com.lpmoon.asset.data.TimeDimension
import com.lpmoon.asset.ui.asset.QrCodeDialog
import com.lpmoon.asset.sync.AssetSyncClient
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(
    assets: List<Asset>,
    totalAssets: Double,
    exchangeRate: ExchangeRate,
    isLoadingExchangeRate: Boolean,
    onAddAsset: (String, String, String, String) -> Unit,
    onUpdateAsset: (Long, String, String, String, String) -> Unit,
    onDeleteAsset: (Long) -> Unit,
    getAssetValueInCny: (Asset) -> Double,
    getAssetDisplayValue: (Asset) -> String,
    onRefreshExchangeRate: () -> Unit,
    getAssetHistory: (Long) -> List<AssetHistory> = { emptyList() },
    getTotalAssetHistory: (TimeDimension) -> List<Pair<String, Double>> = { emptyList() },
    onExportAssets: (Uri) -> Boolean,
    onImportAssets: (Uri) -> Boolean,
    generateDefaultFileName: () -> String = { "assets_export.json" },
    onClearAllAssets: () -> Unit = {},
    getAssetsAsJson: () -> String = { "" },
    onImportFromJson: (String) -> Boolean = { false },
    onSyncSuccess: () -> Unit = {},
    generateSyncQrContent: () -> String? = { null },
    stopSyncServer: () -> Unit = {},
    onNavigateToTaxCalculator: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<Asset?>(null) }
    var selectedAsset by remember { mutableStateOf<Asset?>(null) }
    var showExchangeRateDialog by remember { mutableStateOf(false) }
    var showChartScreen by remember { mutableStateOf(false) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var showQrCodeDialog by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var showQrActionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 导出启动器：创建文档
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            val success = onExportAssets(it)
            val message = if (success) "资产导出成功" else "资产导出失败"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 导入启动器：打开文档
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val success = onImportAssets(it)
            val message = if (success) "资产导入成功" else "资产导入失败，请检查文件格式"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 当资产列表更新时，更新选中的资产
    LaunchedEffect(assets) {
        if (selectedAsset != null) {
            val updatedAsset = assets.find { it.id == selectedAsset!!.id }
            if (updatedAsset != null) {
                selectedAsset = updatedAsset
            }
        }
    }

    if (selectedAsset != null) {
        AssetDetailScreen(
            asset = selectedAsset!!,
            getValueInCny = getAssetValueInCny,
            getDisplayValue = getAssetDisplayValue,
            onBack = { selectedAsset = null },
            onEdit = { editingAsset = selectedAsset!! },
            onDelete = {
                onDeleteAsset(selectedAsset!!.id)
                selectedAsset = null
            },
            histories = getAssetHistory(selectedAsset!!.id)
        )
    } else if (showChartScreen) {
        AssetChartScreen(
            getTotalAssetHistory = getTotalAssetHistory,
            onBack = { showChartScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("个人资产管理") },
                    actions = {
                        Row {
                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "新增资产")
                            }
                            IconButton(
                                onClick = { showExchangeRateDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "汇率")
                            }
                            IconButton(
                                onClick = { showImportExportDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "导入导出")
                            }
                            IconButton(
                                onClick = { showQrActionDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = "二维码")
                            }
                            IconButton(
                                onClick = { showClearConfirmationDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "清空所有资产")
                            }
                        }
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
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                        label = { Text("个人资产") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToTaxCalculator,
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
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            ) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "总资产 (人民币)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¥${DecimalFormat("#,##0.00").format(totalAssets)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            val clipboardManager = LocalClipboardManager.current
                            val totalAssetsText = "¥${DecimalFormat("#,##0.00").format(totalAssets)}"
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(totalAssetsText))
                                    Toast.makeText(context, "已复制: $totalAssetsText", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制总资产", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            IconButton(
                                onClick = { showChartScreen = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Timeline, contentDescription = "资产趋势", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                Spacer(modifier = Modifier.height(4.dp))

                if (assets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无资产，点击右下角按钮添加",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 按资产类型分组
                    val groupedAssets = assets.groupBy { asset ->
                        AssetType.fromString(asset.type)
                    }

                    // 按AssetType枚举顺序显示分组
                    val orderedAssetTypes = AssetType.entries

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        orderedAssetTypes.forEach { assetType ->
                            val assetsInGroup = groupedAssets[assetType]
                            if (!assetsInGroup.isNullOrEmpty()) {
                                // 添加分组标题
                                item {
                                    val totalAmount = assetsInGroup.sumOf { getAssetValueInCny(it) }
                                    AssetTypeHeader(assetType = assetType, totalAmount = totalAmount)
                                }
                                // 添加该分组的资产项
                                items(assetsInGroup) { asset ->
                                    AssetListItem(
                                        asset = asset,
                                        getValueInCny = getAssetValueInCny,
                                        onClick = { selectedAsset = asset }
                                    )
                                }
                                // 在分组之间添加一些间距
                                item {
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AssetDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, value, currency, type ->
                onAddAsset(name, value, currency, type)
                showAddDialog = false
            }
        )
    }

    editingAsset?.let { asset ->
        AssetDialog(
            initialName = asset.name,
            initialValue = asset.value,
            initialCurrency = asset.currency,
            initialType = asset.type,
            onDismiss = { editingAsset = null },
            onConfirm = { name, value, currency, type ->
                onUpdateAsset(asset.id, name, value, currency, type)
                editingAsset = null
            }
        )
    }

    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = { Text("确认清空") },
            text = { Text("确定要清空所有资产数据吗？此操作将删除所有资产、操作记录和总资产快照，且无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllAssets()
                        showClearConfirmationDialog = false
                    }
                ) {
                    Text("确认清空")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmationDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    if (showExchangeRateDialog) {
        ExchangeRateDialog(
            exchangeRate = exchangeRate,
            isLoading = isLoadingExchangeRate,
            onDismiss = { showExchangeRateDialog = false },
            onRefresh = { onRefreshExchangeRate() }
        )
    }

    if (showQrCodeDialog) {
        val qrCodeData = generateSyncQrContent() ?: ""
        QrCodeDialog(
            qrCodeData = qrCodeData,
            onDismiss = {
                showQrCodeDialog = false
                stopSyncServer()
            }
        )
    }

    if (showQrScanner) {
        val context = LocalContext.current
        val assetSyncClient = remember { AssetSyncClient(context) }
        QrScannerScreen(
            onBack = { showQrScanner = false },
            onSyncSuccess = {
                showQrScanner = false
                onSyncSuccess()
            },
            assetSyncClient = assetSyncClient,
            importFromJson = onImportFromJson
        )
    }

    if (showImportExportDialog) {
        AlertDialog(
            onDismissRequest = { showImportExportDialog = false },
            title = { Text("导入导出") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            exportLauncher.launch(generateDefaultFileName())
                            showImportExportDialog = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导出")
                    }
                    Button(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                            showImportExportDialog = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导入")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportExportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showQrActionDialog) {
        AlertDialog(
            onDismissRequest = { showQrActionDialog = false },
            title = { Text("二维码") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            showQrCodeDialog = true
                            showQrActionDialog = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成")
                    }
                    Button(
                        onClick = {
                            showQrScanner = true
                            showQrActionDialog = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("扫描")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQrActionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

}
