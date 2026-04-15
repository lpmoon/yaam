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

class RefreshExchangeRateUseCaseTest {

    private lateinit var exchangeRateRepository: ExchangeRateRepository
    private lateinit var refreshExchangeRateUseCase: RefreshExchangeRateUseCase

    @Before
    fun setUp() {
        exchangeRateRepository = mockk()
        refreshExchangeRateUseCase = RefreshExchangeRateUseCase(exchangeRateRepository)
    }

    @Test
    fun `invoke should call updateExchangeRate on repository`() = runTest {
        // Given
        val updatedRate = ExchangeRate(
            usdToCny = 7.2,
            hkdToCny = 0.92,
            lastUpdateTime = System.currentTimeMillis()
        )
        coEvery { exchangeRateRepository.updateExchangeRate() } returns updatedRate

        // When
        refreshExchangeRateUseCase.invoke()

        // Then
        coVerify { exchangeRateRepository.updateExchangeRate() }
    }

    @Test
    fun `invoke should propagate exception when repository fails`() = runTest {
        // Given
        val expectedException = RuntimeException("Network error")
        coEvery { exchangeRateRepository.updateExchangeRate() } throws expectedException

        // When & Then
        try {
            refreshExchangeRateUseCase.invoke()
            throw AssertionError("Expected exception not thrown")
        } catch (e: RuntimeException) {
            assertEquals(expectedException, e)
        }
    }
}