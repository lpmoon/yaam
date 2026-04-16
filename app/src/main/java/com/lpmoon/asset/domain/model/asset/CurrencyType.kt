package com.lpmoon.asset.domain.model.asset

enum class CurrencyType(val displayName: String, val symbol: String) {
    CNY("人民币", "¥"),
    HKD("港币", "HK$"),
    USD("美元", "$");

    companion object {
        fun fromString(value: String): CurrencyType {
            return values().find { it.name == value } ?: CNY
        }
    }
}