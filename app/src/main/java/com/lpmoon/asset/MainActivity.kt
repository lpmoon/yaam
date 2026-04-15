package com.lpmoon.asset

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lpmoon.asset.presentation.di.ViewModelFactory
import com.lpmoon.asset.presentation.viewmodel.AssetListViewModel
import com.lpmoon.asset.ui.asset.AssetListScreen
import com.lpmoon.asset.ui.asset.AssetRankingScreen
import com.lpmoon.asset.ui.config.ConfigurationScreen
import com.lpmoon.asset.ui.config.TaxSettingsScreen
import com.lpmoon.asset.ui.config.ThemeSettingsScreen
import com.lpmoon.asset.ui.tax.TaxCalculatorScreen
import com.lpmoon.asset.ui.theme.资产管理Theme

sealed class Screen(val title: String) {
    data object AssetList : Screen("个人资产")
    data object TaxCalculator : Screen("税率计算器")
    data object Ranking : Screen("资产排行榜")
    data object Configuration : Screen("配置")
    data object ThemeSettings : Screen("主题设置")
    data object TaxSettings : Screen("税率设置")
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            资产管理Theme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.AssetList) }

                // 使用新的ViewModel架构
                val viewModelFactory = ViewModelFactory(application)
                val newAssetViewModel = viewModelFactory.create(AssetListViewModel::class.java) as AssetListViewModel

                when (currentScreen) {
                    is Screen.AssetList -> {
                        val coroutineScope = rememberCoroutineScope()
                        AssetListScreen(
                            // 逐步迁移到新ViewModel
                            assets = newAssetViewModel.assets.collectAsState().value,
                            totalAssets = newAssetViewModel.totalAssets.collectAsState().value,
                            exchangeRate = newAssetViewModel.exchangeRate.collectAsState().value,
                            isLoadingExchangeRate = newAssetViewModel.isLoadingExchangeRate.collectAsState().value,
                            // 第一步：先迁移添加资产功能到新架构
                            onAddAsset = { name, value, currency, type ->
                                newAssetViewModel.addAsset(name, value, currency, type)
                            },
                            onUpdateAsset = { assetId, name, value, currency, type ->
                                newAssetViewModel.updateAsset(assetId, name, value, currency, type)
                            },
                            onDeleteAsset = { assetId ->
                                newAssetViewModel.deleteAsset(assetId)
                            },
                            getAssetValueInCny = newAssetViewModel::getAssetValueInCny,
                            getAssetDisplayValue = newAssetViewModel::getAssetDisplayValue,
                            onRefreshExchangeRate = {
                                newAssetViewModel.refreshExchangeRate()
                            },
                            getAssetHistory = newAssetViewModel::getAssetHistory,
                            getTotalAssetHistory = newAssetViewModel::getTotalAssetHistory,
                            onExportAssets = newAssetViewModel::exportAssets,
                            onImportAssets = newAssetViewModel::importAssets,
                            onGenerateAssetSnapshot = { context ->
                                // 在协程中调用
                                coroutineScope.launch {
                                    val success = newAssetViewModel.generateAssetSnapshot(context)
                                    val message = if (success) "资产截图已保存到相册" else "截图保存失败"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            generateDefaultFileName = newAssetViewModel::generateDefaultFileName,
                            onClearAllAssets = newAssetViewModel::clearAllAssets,
                            getAssetsAsJson = newAssetViewModel::getAssetsAsJson,
                            onImportFromJson = newAssetViewModel::importFromJson,
                            onSyncSuccess = {},
                            generateSyncQrContent = newAssetViewModel::generateSyncQrContent,
                            stopSyncServer = newAssetViewModel::stopSyncServer,
                            onNavigateToTaxCalculator = { currentScreen = Screen.TaxCalculator },
                            onNavigateToRanking = { currentScreen = Screen.Ranking },
                            onNavigateToConfiguration = { currentScreen = Screen.Configuration }
                        )
                    }
                    is Screen.TaxCalculator -> {
                        TaxCalculatorScreen(
                            onBack = { currentScreen = Screen.AssetList },
                            onNavigateToConfiguration = { currentScreen = Screen.Configuration }
                        )
                    }
                    is Screen.Ranking -> {
                        AssetRankingScreen(
                            assets = newAssetViewModel.assets.collectAsState().value,
                            totalAssets = newAssetViewModel.totalAssets.collectAsState().value,
                            getAssetValueInCny = newAssetViewModel::getAssetValueInCny,
                            onBack = { currentScreen = Screen.AssetList },
                            onNavigateToConfiguration = { currentScreen = Screen.Configuration }
                        )
                    }
                    is Screen.Configuration -> {
                        ConfigurationScreen(
            onBack = { currentScreen = Screen.AssetList },
                            onNavigateToAssetList = { currentScreen = Screen.AssetList },
                            onNavigateToTaxCalculator = { currentScreen = Screen.TaxCalculator },
                            onNavigateToThemeSettings = { currentScreen = Screen.ThemeSettings },
                            onNavigateToTaxSettings = { currentScreen = Screen.TaxSettings }
                        )
                    }
                    is Screen.ThemeSettings -> {
                        ThemeSettingsScreen(
            onBack = { currentScreen = Screen.Configuration }
                        )
                    }
                    is Screen.TaxSettings -> {
                        TaxSettingsScreen(
            onBack = { currentScreen = Screen.Configuration }
                        )
                    }
                }
            }
        }
    }
}
