package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.repository.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClearAllAssetsUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var clearAllAssetsUseCase: ClearAllAssetsUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        clearAllAssetsUseCase = ClearAllAssetsUseCase(assetRepository)
    }

    @Test
    fun `invoke should call clearAllData on repository`() = runTest {
        // Given
        coEvery { assetRepository.clearAllData() } coAnswers { }

        // When
        clearAllAssetsUseCase.invoke()

        // Then
        coVerify { assetRepository.clearAllData() }
    }

    @Test
    fun `invoke should succeed when repository succeeds`() = runTest {
        // Given
        coEvery { assetRepository.clearAllData() } coAnswers { }

        // When & Then (should not throw)
        clearAllAssetsUseCase.invoke()
    }

    @Test
    fun `invoke should propagate exception when repository fails`() = runTest {
        // Given
        val expectedException = RuntimeException("Database error")
        coEvery { assetRepository.clearAllData() } throws expectedException

        // When & Then
        try {
            clearAllAssetsUseCase.invoke()
            throw AssertionError("Expected exception not thrown")
        } catch (e: RuntimeException) {
            assertEquals(expectedException, e)
        }
    }
}