package com.lpmoon.asset.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 汇率API数据源
 * 负责从汇率API获取数据
 */
class ExchangeRateApiDataSource {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        private const val TAG = "ExchangeRateApiDataSource"
        private const val BASE_URL = "https://www.huilvbiao.com"
    }

    /**
     * 从汇率表API获取汇率数据
     * @return Pair<美元汇率, 港币汇率>，如果获取失败返回 null
     */
    suspend fun fetchExchangeRates(): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val usdRate = fetchSingleRate("USD")
            val hkdRate = fetchSingleRate("HKD")

            if (usdRate == null || hkdRate == null) {
                Log.e(TAG, "解析汇率失败: USD=$usdRate, HKD=$hkdRate")
                return@withContext null
            }

            Log.d(TAG, "从API获取汇率成功: USD=$usdRate, HKD=$hkdRate")
            Pair(usdRate, hkdRate)
        } catch (e: Exception) {
            Log.e(TAG, "从API获取汇率失败", e)
            null
        }
    }

    /**
     * 获取单个货币对CNY的汇率
     */
    private suspend fun fetchSingleRate(fromCode: String): Double? = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "$BASE_URL/transform?money=1&fromcode=$fromCode&tocode=CNY"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val jsonContent = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "汇率API请求失败: ${response.code}, URL=$url")
                return@withContext null
            }

            val jsonObject = gson.fromJson(jsonContent, JsonObject::class.java)
            val rate = jsonObject.get("rate")?.asDouble

            if (rate == null) {
                Log.e(TAG, "解析汇率失败: 找不到rate字段, URL=$url")
                return@withContext null
            }

            Log.d(TAG, "从API获取${fromCode}汇率成功: $rate")
            rate
        } catch (e: Exception) {
            Log.e(TAG, "获取${fromCode}汇率失败", e)
            null
        }
    }
}