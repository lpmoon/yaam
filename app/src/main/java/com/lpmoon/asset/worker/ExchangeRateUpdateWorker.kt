package com.lpmoon.asset.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lpmoon.asset.network.ExchangeRateService

/**
 * 定时更新汇率的 Worker
 * 每1小时执行一次
 */
class ExchangeRateUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val service = ExchangeRateService(applicationContext)
            service.updateExchangeRate()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "exchange_rateUpdate"
    }
}
