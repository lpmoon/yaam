package com.lpmoon.asset.ui.asset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpmoon.asset.domain.model.asset.Asset
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetRankingScreen(
    assets: List<Asset>,
    totalAssets: Double,
    getAssetValueInCny: (Asset) -> Double,
    onBack: () -> Unit,
    onNavigateToConfiguration: () -> Unit = {}
) {
    // 计算每个资产的价值和百分比，并按价值从高到低排序
    val rankedAssets = remember(assets, totalAssets) {
        if (totalAssets > 0) {
            assets.map { asset ->
                val value = getAssetValueInCny(asset)
                val percentage = value / totalAssets
                RankedAsset(asset, value, percentage, 0) // 临时排名0，将在排序后更新
            }.sortedByDescending { it.value }
            .mapIndexed { index, rankedAsset ->
                rankedAsset.copy(rank = index + 1) // 设置正确排名（从1开始）
            }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资产排行榜") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                    selected = false,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 总资产卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "总资产 (人民币)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${DecimalFormat("#,##0.00").format(totalAssets)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (rankedAssets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无资产数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 排行榜列表
                items(rankedAssets) { rankedAsset ->
                    RankingItem(
                        rankedAsset = rankedAsset
                    )
                }
            }
        }
    }
}

/**
 * 排名资产数据类
 */
data class RankedAsset(
    val asset: Asset,
    val value: Double,
    val percentage: Double,
    val rank: Int
)

/**
 * 排行榜项目
 */
@Composable
fun RankingItem(
    rankedAsset: RankedAsset,
    modifier: Modifier = Modifier
) {
    val decimalFormat = DecimalFormat("#,##0.00")
    val percentageFormat = DecimalFormat("#0.0%")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 1.dp)
        ) {
            // 资产名称、排名和价值
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${rankedAsset.rank}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = rankedAsset.asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "¥${decimalFormat.format(rankedAsset.value)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 横向柱状图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                // 预定义的颜色列表（Material Design调色板）
                val predefinedColors = remember {
                    listOf(
                        Color(0xFF2196F3), // 蓝色
                        Color(0xFF4CAF50), // 绿色
                        Color(0xFFFF9800), // 橙色
                        Color(0xFFF44336), // 红色
                        Color(0xFF9C27B0), // 紫色
                        Color(0xFF00BCD4), // 青色
                        Color(0xFF795548), // 棕色
                        Color(0xFF607D8B), // 蓝灰色
                        Color(0xFF3F51B5), // 靛蓝色
                        Color(0xFFE91E63), // 粉色
                        Color(0xFFCDDC39), // 黄绿色
                        Color(0xFFFFC107), // 琥珀色
                    )
                }

                // 根据资产ID选择颜色，确保同一个资产总是显示相同颜色
                val barColor = remember(rankedAsset.asset.id, rankedAsset.asset.name) {
                    val hash = if (rankedAsset.asset.id != 0L) {
                        rankedAsset.asset.id.hashCode()
                    } else {
                        rankedAsset.asset.name.hashCode()
                    }
                    val index = hash % predefinedColors.size
                    predefinedColors[if (index < 0) -index else index]
                }

                // 判断颜色是否为亮色的扩展函数
                fun Color.isLightColor(): Boolean {
                    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
                    return luminance > 0.5f
                }

                // 根据柱状图颜色自动计算合适的文本颜色
                val textColor = if (barColor.isLightColor()) Color.Black else Color.White
                val backgroundColor = MaterialTheme.colorScheme.outlineVariant

                // 背景
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = backgroundColor,
                        topLeft = Offset.Zero,
                        size = size
                    )
                }

                // 柱状图（百分比）
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width * rankedAsset.percentage.toFloat()
                    drawRect(
                        color = barColor,
                        topLeft = Offset.Zero,
                        size = Size(barWidth, size.height)
                    )
                }

                // 百分比标签
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // 我们需要计算barWidth来检查是否显示文本
                    // 但由于size只在Canvas lambda中可用，我们使用一个占位符检查
                    // 实际上，百分比足够大时应该显示文本
                    if (rankedAsset.percentage > 0.1) { // 只在百分比大于10%时显示文本
                        Text(
                            text = percentageFormat.format(rankedAsset.percentage),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}