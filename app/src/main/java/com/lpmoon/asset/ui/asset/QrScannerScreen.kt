package com.lpmoon.asset.ui.asset

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lpmoon.asset.data.asset.AssetImportExportService
import com.lpmoon.asset.sync.AssetSyncClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    onSyncSuccess: () -> Unit,
    assetSyncClient: AssetSyncClient,
    importFromJson: (String) -> Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 处理二维码内容的函数
    val handleQrContent = { qrContent: String ->
        isLoading = true
        errorMessage = null

        scope.launch {
            // 解析二维码内容
            val syncInfo = assetSyncClient.parseQrContent(qrContent)
            if (syncInfo == null) {
                errorMessage = "无效的二维码格式"
                isLoading = false
                Toast.makeText(context, "无效的二维码格式", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 下载资产数据
            assetSyncClient.downloadAssets(syncInfo, object : AssetSyncClient.SyncCallback {
                override fun onSuccess(assets: List<AssetImportExportService.ExportAsset>) {
                    // 转换为JSON
                    val gson = Gson()
                    val json = gson.toJson(assets)

                    // 导入数据
                    val success = importFromJson(json)
                    if (success) {
                        Toast.makeText(context, "成功导入 ${assets.size} 条资产", Toast.LENGTH_SHORT).show()
                        onSyncSuccess()
                    } else {
                        errorMessage = "导入数据失败"
                        Toast.makeText(context, "导入数据失败", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }

                override fun onFailure(errorMsg: String) {
                    errorMessage = errorMsg
                    isLoading = false
                    Toast.makeText(context, "同步失败: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // 二维码扫描启动器
    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents == null) {
            // 用户取消了扫描
            isLoading = false
            return@rememberLauncherForActivityResult
        }

        val qrContent = result.contents
        handleQrContent(qrContent)
    }

    // 启动二维码扫描
    fun launchQrScanner() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("扫描资产同步二维码")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(false)
        options.setOrientationLocked(false)

        qrScannerLauncher.launch(options)
    }

    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchQrScanner()
        } else {
            errorMessage = "需要相机权限才能扫描二维码"
            Toast.makeText(context, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
        }
    }

    // 检查权限并开始扫描
    fun checkPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchQrScanner()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // 启动时自动开始扫描
    LaunchedEffect(Unit) {
        checkPermissionAndScan()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描二维码") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("正在同步资产数据...")
            } else {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "二维码扫描",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "将二维码放入框内扫描",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "确保两台设备在同一Wi-Fi网络",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { checkPermissionAndScan() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重新扫描")
                }
            }

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}