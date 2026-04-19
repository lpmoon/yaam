package com.lpmoon.asset.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 格式化数字为字符串，去除不必要的精度
 */
fun formatNumber(value: Double): String {
    val decimal = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    return if (decimal.scale() < 0) {
        decimal.toBigInteger().toString()
    } else {
        decimal.toString()
    }
}
