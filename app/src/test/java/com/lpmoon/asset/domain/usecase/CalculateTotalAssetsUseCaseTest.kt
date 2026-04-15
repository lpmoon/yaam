package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.ExchangeRate
import com.lpmoon.asset.domain.repository.AssetRepository
import com.lpmoon.asset.domain.repository.ExchangeRateRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateTotalAssetsUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var exchangeRateRepository: ExchangeRateRepository
    private lateinit var calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        exchangeRateRepository = mockk()
        calculateTotalAssetsUseCase = CalculateTotalAssetsUseCase(assetRepository, exchangeRateRepository)
    }

    @Test
    fun `invoke should combine asset and exchange rate flows`() = runTest {
        // Given
        val assets = listOf(
            Asset(id = 1L, name = "Asset1", value = "1000", currency = "CNY", type = "Cash"),
            Asset(id = 2L, name = "Asset2", value = "100", currency = "USD", type = "Stock")
        )
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        coEvery { assetRepository.getAllAssets() } returns flowOf(assets)
        coEvery { exchangeRateRepository.getCachedExchangeRate() } returns exchangeRate

        // When
        val resultFlow = calculateTotalAssetsUseCase.invoke()
        val result = resultFlow.take(1).toList()

        // Then
        assertEquals(1, result.size)
        // 1000 CNY + 100 USD * 7.2 = 1000 + 720 = 1720
        assertEquals(1720.0, result[0], 0.001)
    }

    @Test
    fun `invoke should update when assets change`() = runTest {
        // Given
        val exchangeRate = ExchangeRate.getDefaultValues()
        coEvery { exchangeRateRepository.getCachedExchangeRate() } returns exchangeRate

        val firstAssets = listOf(
            Asset(id = 1L, name = "Asset1", value = "500", currency = "CNY", type = "Cash")
        )
        val secondAssets = listOf(
            Asset(id = 1L, name = "Asset1", value = "500", currency = "CNY", type = "Cash"),
            Asset(id = 2L, name = "Asset2", value = "200", currency = "USD", type = "Stock")
        )

        // Simulate asset changes by emitting multiple values
        val assetFlow = kotlinx.coroutines.flow.flow {
            emit(firstAssets)
            emit(secondAssets)
        }
        coEvery { assetRepository.getAllAssets() } returns assetFlow

        // When
        val resultFlow = calculateTotalAssetsUseCase.invoke()
        val results = resultFlow.take(2).toList()

        // Then
        assertEquals(2, results.size)
        // First: 500 CNY = 500
        assertEquals(500.0, results[0], 0.001)
        // Second: 500 CNY + 200 USD * 7.2 = 500 + 1440 = 1940
        assertEquals(1940.0, results[1], 0.001)
    }

    @Test
    fun `invoke should handle empty asset list`() = runTest {
        // Given
        val emptyAssets = emptyList<Asset>()
        val exchangeRate = ExchangeRate.getDefaultValues()
        coEvery { assetRepository.getAllAssets() } returns flowOf(emptyAssets)
        coEvery { exchangeRateRepository.getCachedExchangeRate() } returns exchangeRate

        // When
        val resultFlow = calculateTotalAssetsUseCase.invoke()
        val result = resultFlow.take(1).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(0.0, result[0], 0.001)
    }

    @Test
    fun `invoke should handle exchange rate failure gracefully`() = runTest {
        // Given
        val assets = listOf(Asset(id = 1L, name = "Asset", value = "1000", currency = "CNY", type = "Cash"))
        coEvery { assetRepository.getAllAssets() } returns flowOf(assets)
        // First call fails, second call returns default
        var callCount = 0
        coEvery { exchangeRateRepository.getCachedExchangeRate() } answers {
            callCount++
            if (callCount == 1) {
                throw RuntimeException("Network error")
            } else {
                ExchangeRate.getDefaultValues()
            }
        }

        // When
        val resultFlow = calculateTotalAssetsUseCase.invoke()
        // Take first 2 emissions (first after failure, second after recovery)
        val results = resultFlow.take(2).toList()

        // Then: Should emit default exchange rate after failure
        assertEquals(2, results.size)
        // Both should be 1000 (CNY)
        assertEquals(1000.0, results[0], 0.001)
        assertEquals(1000.0, results[1], 0.001)
    }

    @Test
    fun `invoke should handle complex expressions in asset values`() = runTest {
        // Given
        val assets = listOf(
            Asset(id = 1L, name = "Asset1", value = "100+200", currency = "CNY", type = "Cash"), // 300
            Asset(id = 2L, name = "Asset2", value = "50*2", currency = "USD", type = "Stock") // 100
        )
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        coEvery { assetRepository.getAllAssets() } returns flowOf(assets)
        coEvery { exchangeRateRepository.getCachedExchangeRate() } returns exchangeRate

        // When
        val resultFlow = calculateTotalAssetsUseCase.invoke()
        val result = resultFlow.take(1).toList()

        // Then
        // 300 CNY + 100 USD * 7.2 = 300 + 720 = 1020
        assertEquals(1020.0, result[0], 0.001)
    }
}