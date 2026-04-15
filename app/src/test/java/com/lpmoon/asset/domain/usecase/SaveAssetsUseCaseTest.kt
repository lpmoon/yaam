package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.repository.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveAssetsUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var saveAssetsUseCase: SaveAssetsUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        saveAssetsUseCase = SaveAssetsUseCase(assetRepository)
    }

    @Test
    fun `invoke should call saveAssets on repository`() = runTest {
        // Given
        val assets = listOf(
            Asset(id = 1L, name = "Asset1", value = "1000", currency = "CNY", type = "Cash"),
            Asset(id = 2L, name = "Asset2", value = "2000", currency = "USD", type = "Stock")
        )
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }

        // When
        saveAssetsUseCase.invoke(assets)

        // Then
        coVerify { assetRepository.saveAssets(assets) }
    }

    @Test
    fun `invoke should handle empty asset list`() = runTest {
        // Given
        val emptyAssets = emptyList<Asset>()
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }

        // When
        saveAssetsUseCase.invoke(emptyAssets)

        // Then
        coVerify { assetRepository.saveAssets(emptyAssets) }
    }

    @Test
    fun `invoke should propagate exception when repository fails`() = runTest {
        // Given
        val assets = listOf(Asset(id = 1L, name = "Asset", value = "100", currency = "CNY", type = "Cash"))
        val expectedException = RuntimeException("Database error")
        coEvery { assetRepository.saveAssets(any()) } throws expectedException

        // When & Then
        try {
            saveAssetsUseCase.invoke(assets)
            throw AssertionError("Expected exception not thrown")
        } catch (e: RuntimeException) {
            org.junit.Assert.assertEquals(expectedException, e)
        }
    }
}