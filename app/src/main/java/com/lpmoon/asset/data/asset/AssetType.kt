package com.lpmoon.asset.data.asset

enum class AssetType(val displayName: String) {
    BANK_DEPOSIT("银行存款"),
    ALIPAY("支付宝"),
    WECHAT("微信"),
    STOCK("股票"),
    OPTION("期权"),
    OTHER("其他");

    companion object {
        fun fromString(value: String): AssetType {
            return values().find { it.name == value } ?: OTHER
        }
    }
}