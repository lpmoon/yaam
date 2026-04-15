package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.ExchangeRate
import com.lpmoon.asset.domain.repository.ExchangeRateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetExchangeRateUseCaseTest {

    private lateinit var exchangeRateRepository: ExchangeRateRepository
    private lateinit var getExchangeRateUseCase: GetExchangeRateUseCase

    @Before
    fun setUp() {
        exchangeRateRepository = mockk()
        getExchangeRateUseCase = GetExchangeRateUseCase(exchangeRateRepository)
    }

    @Test
    fun `invoke should return cached exchange rate from repository`() = runTest {
        // Given
        val expectedRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        coEvery { exchangeRateRepository.getCachedExchangeRate() } returns expectedRate

        // When
        val result = getExchangeRateUseCase.invoke()

        // Then
        assertEquals(expectedRate, result)
        coVerify { exchangeRateRepository.getCachedExchangeRate() }
    }

    @Test
    fun `invoke should propagate exception when repository fails`() = runTest {
        // Given
        val expectedException = RuntimeException("Network error")
        coEvery { exchangeRateRepository.getCachedExchangeRate() } throws expectedException

        // When & Then
        try {
            getExchangeRateUseCase.invoke()
            throw AssertionError("Expected exception not thrown")
        } catch (e: RuntimeException) {
            assertEquals(expectedException, e)
        }
    }
}