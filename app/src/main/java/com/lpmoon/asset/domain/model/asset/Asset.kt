package com.lpmoon.asset.domain.model.asset

/**
 * 资产领域模型
 * @param id 资产ID
 * @param name 资产名称
 * @param value 资产值（支持四则运算表达式）
 * @param currency 货币类型
 * @param type 资产类型
 */
data class Asset(
    val id: Long = 0,
    val name: String,
    val value: String,  // 改为字符串，支持四则运算表达式
    val currency: String = "CNY",
    val type: String = "OTHER"
)