package com.lpmoon.asset.domain.model.asset

/**
 * 资产数据传输对象
 * 用于文件导入导出和二维码同步
 */
data class ExportAsset(
    val name: String,
    val type: String,
    val value: String,
    val currency: String
)