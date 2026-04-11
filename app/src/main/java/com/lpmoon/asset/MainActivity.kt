package com.lpmoon.asset

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lpmoon.asset.ui.AssetListScreen
import com.lpmoon.asset.ui.theme.资产管理Theme
import com.lpmoon.asset.viewmodel.AssetViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            资产管理Theme {
                val viewModel: AssetViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            AssetViewModel(this@MainActivity.applicationContext as android.app.Application)
                        }
                    }
                )
                val assets by viewModel.assets.collectAsState()
                val totalAssets by viewModel.totalAssets.collectAsState()
                val exchangeRate by viewModel.exchangeRate.collectAsState()
                val isLoadingExchangeRate by viewModel.isLoadingExchangeRate.collectAsState()

                AssetListScreen(
                    assets = assets,
                    totalAssets = totalAssets,
                    exchangeRate = exchangeRate,
                    isLoadingExchangeRate = isLoadingExchangeRate,
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
                    stopSyncServer = viewModel::stopSyncServer
                )
            }
        }
    }
}
