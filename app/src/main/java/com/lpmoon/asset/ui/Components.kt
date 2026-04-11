package com.lpmoon.asset.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lpmoon.asset.data.Asset
import com.lpmoon.asset.data.AssetHistory
import com.lpmoon.asset.data.AssetType
import com.lpmoon.asset.data.CurrencyType
import java.text.DecimalFormat

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