package com.lpmoon.asset.ui.asset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.AssetType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 根据资产类型和名称获取图标
 *
 * 注意：如果你想使用银行的实际Logo图标，可以按照以下步骤操作：
 * 1. 将银行Logo文件（建议使用SVG格式）放在 `res/drawable/` 目录下
 *    - 中国银行：ic_bank_boc (Bank of China)
 *    - 招商银行：ic_bank_cmb (China Merchants Bank)
 *    - 汇丰银行：ic_bank_hsbc (HSBC)
 * 2. 修改此函数，先尝试加载对应的drawable资源，如果不存在再回退到Material Design图标
 *
 * 目前暂时使用不同的Material Design图标来区分不同银行：
 *  - 中国银行：🏦 AccountBalance (银行建筑)
 *  - 招商银行：💳 CreditCard (信用卡)
 *  - 汇丰银行：🏢 CorporateFare (企业建筑)
 */
@Composable
fun getAssetIcon(asset: Asset): ImageVector {
    val assetType = AssetType.fromString(asset.type)
    return when (assetType) {
        AssetType.BANK_DEPOSIT -> {
            // 根据银行名称返回不同图标
            // 如需使用真实银行Logo，请按照上述注释操作
            when (asset.name) {
                "中国银行" -> Icons.Filled.AccountBalance
                "招商银行" -> Icons.Filled.CreditCard
                "汇丰银行" -> Icons.Filled.CorporateFare
                else -> Icons.Filled.AccountBalance
            }
        }
        AssetType.ALIPAY -> Icons.Filled.Payment
        AssetType.WECHAT -> Icons.Filled.Chat
        AssetType.STOCK -> Icons.Filled.TrendingUp
        AssetType.OPTION -> Icons.Filled.Settings
        AssetType.OTHER -> Icons.Filled.Category
    }
}

@Composable
fun AssetListItem(
    asset: Asset,
    getValueInCny: (Asset) -> Double,
    onClick: () -> Unit
) {
    val cnyValue = getValueInCny(asset)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getAssetIcon(asset),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${DecimalFormat("#,##0.00").format(cnyValue)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AssetTypeHeader(assetType: AssetType, totalAmount: Double = 0.0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = assetType.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${DecimalFormat("#,##0.00").format(totalAmount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9E9E9E)
        )
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    valueFontWeight: androidx.compose.ui.text.font.FontWeight? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            fontWeight = valueFontWeight
        )
    }
}

@Composable
fun HistoryItem(history: AssetHistory) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = history.getDescription(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 资产快照内容组件 —— 总资产卡片 + 按类型分组的资产列表。
 *
 * 该组件同时被 [AssetListScreen] 的正常视图和截图场景复用，
 * 以确保截图内容与屏幕显示完全一致。
 *
 * @param assets              资产列表
 * @param totalAssets         总资产（人民币）
 * @param getAssetValueInCny  将资产换算为人民币的函数
 * @param modifier            外部传入的 Modifier，用于截图时指定捕获区域
 * @param showTimestamp       是否在底部显示生成时间水印（截图时为 true）
 * @param onAssetClick        点击单个资产条目的回调；截图场景传 null 表示不可点击
 * @param cardActions         总资产卡片内金额右侧的额外操作按钮（复制/趋势/排行榜等）；
 *                            截图场景不传，保持卡片干净
 */
@Composable
fun AssetSnapshotContent(
    assets: List<Asset>,
    totalAssets: Double,
    getAssetValueInCny: (Asset) -> Double,
    modifier: Modifier = Modifier,
    showTimestamp: Boolean = false,
    onAssetClick: ((Asset) -> Unit)? = null,
    cardActions: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        // ---- 总资产卡片 ----
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¥${DecimalFormat("#,##0.00").format(totalAssets)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    // 主屏下显示操作按钮；截图时 cardActions 为 null，不渲染
                    cardActions?.invoke()
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---- 资产分组列表 ----
        if (assets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无资产",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val groupedAssets = assets.groupBy { AssetType.fromString(it.type) }
            val orderedAssetTypes = AssetType.entries

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                orderedAssetTypes.forEach { assetType ->
                    val assetsInGroup = groupedAssets[assetType]
                    if (!assetsInGroup.isNullOrEmpty()) {
                        val totalAmount = assetsInGroup.sumOf { getAssetValueInCny(it) }
                        AssetTypeHeader(assetType = assetType, totalAmount = totalAmount)
                        assetsInGroup.forEach { asset ->
                            key(asset.id) {
                                AssetListItem(
                                    asset = asset,
                                    getValueInCny = getAssetValueInCny,
                                    onClick = { onAssetClick?.invoke(asset) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        // ---- 可选时间水印 ----
        if (showTimestamp) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "生成时间：${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
