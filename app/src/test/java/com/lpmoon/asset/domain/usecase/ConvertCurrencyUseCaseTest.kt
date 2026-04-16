package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.asset.ExchangeRate
import com.lpmoon.asset.domain.usecase.asset.ConvertCurrencyUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertCurrencyUseCaseTest {

    private val useCase = ConvertCurrencyUseCase()

    @Test
    fun `invoke should convert USD to CNY correctly`() = runTest {
        // Given
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        val params = ConvertCurrencyUseCase.Params(
            amount = 100.0,
            currency = "USD",
            exchangeRate = exchangeRate
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertEquals(720.0, result, 0.001)
    }

    @Test
    fun `invoke should convert HKD to CNY correctly`() = runTest {
        // Given
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        val params = ConvertCurrencyUseCase.Params(
            amount = 100.0,
            currency = "HKD",
            exchangeRate = exchangeRate
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertEquals(92.0, result, 0.001)
    }

    @Test
    fun `invoke should return same amount for CNY`() = runTest {
        // Given
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        val params = ConvertCurrencyUseCase.Params(
            amount = 100.0,
            currency = "CNY",
            exchangeRate = exchangeRate
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `invoke should treat blank currency as CNY`() = runTest {
        // Given
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        val params = ConvertCurrencyUseCase.Params(
            amount = 100.0,
            currency = "",
            exchangeRate = exchangeRate
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `invoke should return same amount for unsupported currency`() = runTest {
        // Given
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        val params = ConvertCurrencyUseCase.Params(
            amount = 100.0,
            currency = "EUR", // Not supported in conversion
            exchangeRate = exchangeRate
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `invoke should handle zero amount`() = runTest {
        // Given
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        val params = ConvertCurrencyUseCase.Params(
            amount = 0.0,
            currency = "USD",
            exchangeRate = exchangeRate
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `invoke should handle negative amount`() = runTest {
        // Given
        val exchangeRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        val params = ConvertCurrencyUseCase.Params(
            amount = -100.0,
            currency = "USD",
            exchangeRate = exchangeRate
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertEquals(-720.0, result, 0.001)
    }
}