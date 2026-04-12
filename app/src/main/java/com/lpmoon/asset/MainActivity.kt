package com.lpmoon.asset

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lpmoon.asset.ui.asset.AssetListScreen
import com.lpmoon.asset.ui.asset.AssetRankingScreen
import com.lpmoon.asset.ui.config.ConfigurationScreen
import com.lpmoon.asset.ui.config.TaxSettingsScreen
import com.lpmoon.asset.ui.config.ThemeSettingsScreen
import com.lpmoon.asset.ui.tax.TaxCalculatorScreen
import com.lpmoon.asset.ui.theme.资产管理Theme
import com.lpmoon.asset.viewmodel.AssetViewModel

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

                val viewModel: AssetViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            AssetViewModel(this@MainActivity.applicationContext as android.app.Application)
                        }
                    }
                )

                when (currentScreen) {
                    is Screen.AssetList -> {
                        AssetListScreen(
                            assets = viewModel.assets.collectAsState().value,
                            totalAssets = viewModel.totalAssets.collectAsState().value,
                            exchangeRate = viewModel.exchangeRate.collectAsState().value,
                            isLoadingExchangeRate = viewModel.isLoadingExchangeRate.collectAsState().value,
                            onAddAsset = viewModel::addAsset,
                            onUpdateAsset = viewModel::updateAsset,
                            onDeleteAsset = viewModel::deleteAsset,
                            getAssetValueInCny = viewModel::getAssetValueInCny,
                            getAssetDisplayValue = viewModel::getAssetDisplayValue,
                            onRefreshExchangeRate = viewModel::refreshExchangeRate,
                            getAssetHistory = viewModel::getAssetHistory,
                            getTotalAssetHistory = viewModel::getTotalAssetHistory,
                            onExportAssets = viewModel::exportAssets,
                            onImportAssets = viewModel::importAssets,
                            generateDefaultFileName = viewModel::generateDefaultFileName,
                            onClearAllAssets = viewModel::clearAllAssets,
                            getAssetsAsJson = viewModel::getAssetsAsJson,
                            onImportFromJson = viewModel::importFromJson,
                            onSyncSuccess = {},
                            generateSyncQrContent = viewModel::generateSyncQrContent,
                            stopSyncServer = viewModel::stopSyncServer,
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
                            assets = viewModel.assets.collectAsState().value,
                            totalAssets = viewModel.totalAssets.collectAsState().value,
                            getAssetValueInCny = viewModel::getAssetValueInCny,
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
