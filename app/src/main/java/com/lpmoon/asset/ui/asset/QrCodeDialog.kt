package com.lpmoon.asset.ui.asset

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lpmoon.asset.util.QrCodeGenerator

@Composable
fun QrCodeDialog(
    qrCodeData: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var qrCodeBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(qrCodeData) {
        if (qrCodeData.isNotEmpty()) {
            val bitmap = QrCodeGenerator.generateQrCodeBitmap(qrCodeData)
            if (bitmap != null) {
                qrCodeBitmap = bitmap
                errorMessage = null
            } else {
                errorMessage = "生成二维码失败"
            }
            isLoading = false
        } else {
            errorMessage = "无数据可生成二维码"
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "资产同步二维码",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator()
                        }
                        errorMessage != null -> {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        qrCodeBitmap != null -> {
                            Image(
                                bitmap = qrCodeBitmap!!,
                                contentDescription = "资产同步二维码",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Text(
                                text = "未知错误",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "数据长度: ${qrCodeData.length} 字符",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (qrCodeData.length > 4000) {
                    Text(
                        text = "注意：数据接近二维码容量上限",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("关闭")
                }
            }
        }
    }
}