package com.lpmoon.asset.data

data class Asset(
    val id: Long = 0,
    val name: String,
    val value: String,  // 改为字符串，支持四则运算表达式
    val currency: String = CurrencyType.CNY.name,
    val type: String = AssetType.OTHER.name
)
