package com.lpmoon.asset.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lpmoon.asset.data.asset.ExchangeRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


class ExchangeRateService(private val context: android.content.Context) {


    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val sharedPreferences = context.getSharedPreferences("exchange_rate_prefs", android.content.Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USD_TO_CNY = "usd_to_cny"
        private const val KEY_HKD_TO_CNY = "hkd_to_cny"
        private const val KEY_LAST_UPDATE = "last_update_time"
    }

    /**
     * 获取当前汇率
     * 如果过期则自动更新
     */
    suspend fun getExchangeRate(): ExchangeRate = withContext(Dispatchers.IO) {
        val cached = getCachedExchangeRate()

        // 如果缓存未过期，直接返回
        if (!cached.isExpired()) {
            return@withContext cached
        }

        // 否则尝试更新
        return@withContext try {
            updateExchangeRate()
        } catch (e: Exception) {
            // 更新失败，返回缓存的汇率
            cached
        }
    }



    /**
     * 从汇率表API获取汇率数据
     * @return Pair<美元汇率, 港币汇率>，如果获取失败返回 null
     */
    private suspend fun fetchExchangeRatesFromJsonApi(): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val usdRate = fetchSingleRate("USD")
            val hkdRate = fetchSingleRate("HKD")

            if (usdRate == null || hkdRate == null) {
                Log.e("ExchangeRate", "解析汇率失败: USD=$usdRate, HKD=$hkdRate")
                return@withContext null
            }

            Log.d("ExchangeRate", "从API获取汇率成功: USD=$usdRate, HKD=$hkdRate")
            Pair(usdRate, hkdRate)
        } catch (e: Exception) {
            Log.e("ExchangeRate", "从API获取汇率失败", e)
            null
        }
    }

    /**
     * 获取单个货币对CNY的汇率
     */
    private suspend fun fetchSingleRate(fromCode: String): Double? = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "https://www.huilvbiao.com/transform?money=1&fromcode=$fromCode&tocode=CNY"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val jsonContent = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("ExchangeRate", "汇率API请求失败: ${response.code}, URL=$url")
                return@withContext null
            }

            val gson = Gson()
            val jsonObject = gson.fromJson(jsonContent, JsonObject::class.java)
            val rate = jsonObject.get("rate")?.asDouble

            if (rate == null) {
                Log.e("ExchangeRate", "解析汇率失败: 找不到rate字段, URL=$url")
                return@withContext null
            }

            Log.d("ExchangeRate", "从API获取${fromCode}汇率成功: $rate")
            rate
        } catch (e: Exception) {
            Log.e("ExchangeRate", "获取${fromCode}汇率失败", e)
            null
        }
    }

    /**
     * 强制更新汇率 - 使用中国货币网 JSON API，失败时使用固定值
     */
    suspend fun updateExchangeRate(): ExchangeRate = withContext(Dispatchers.IO) {
        try {
            Log.d("ExchangeRate", "开始获取汇率...")

            // 尝试从 JSON API 获取汇率
            val jsonRates = fetchExchangeRatesFromJsonApi()

            val (usdToCny, hkdToCny) = if (jsonRates != null) {
                Log.d("ExchangeRate", "从JSON API获取汇率成功: USD=${jsonRates.first}, HKD=${jsonRates.second}")
                jsonRates
            } else {
                Log.d("ExchangeRate", "JSON API获取失败，使用默认汇率")
                Pair(7.2, 0.92) // 默认值
            }

            Log.d("ExchangeRate", "美元汇率: 1美元 = $usdToCny 人民币")
            Log.d("ExchangeRate", "港币汇率: 1港币 = $hkdToCny 人民币")

            val newRate = ExchangeRate(
                usdToCny = usdToCny,
                hkdToCny = hkdToCny,
                lastUpdateTime = System.currentTimeMillis()
            )

            saveExchangeRate(newRate)
            Log.d("ExchangeRate", "汇率更新成功: $newRate")
            newRate
        } catch (e: Exception) {
            Log.e("ExchangeRate", "获取汇率失败，使用默认值", e)
            ExchangeRate.getDefaultValues()
        }
    }

    private suspend fun getCachedExchangeRate(): ExchangeRate = withContext(Dispatchers.IO) {
        val usdToCny = sharedPreferences.getFloat(KEY_USD_TO_CNY, 7.2f).toDouble()
        val hkdToCny = sharedPreferences.getFloat(KEY_HKD_TO_CNY, 0.92f).toDouble()
        val lastUpdateTime = sharedPreferences.getLong(KEY_LAST_UPDATE, 0)

        ExchangeRate(usdToCny, hkdToCny, lastUpdateTime)
    }

    private suspend fun saveExchangeRate(rate: ExchangeRate) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().apply {
            putFloat(KEY_USD_TO_CNY, rate.usdToCny.toFloat())
            putFloat(KEY_HKD_TO_CNY, rate.hkdToCny.toFloat())
            putLong(KEY_LAST_UPDATE, rate.lastUpdateTime)
            apply()
        }
    }

    /**
     * 将指定货币的金额转换为人民币
     */
    fun convertToCny(amount: Double, currency: String, exchangeRate: ExchangeRate): Double {
        val currencyType = if (currency.isBlank()) "CNY" else currency
        return when (currencyType) {
            "CNY" -> amount
            "USD" -> amount * exchangeRate.usdToCny
            "HKD" -> amount * exchangeRate.hkdToCny
            else -> amount
        }
    }
}
