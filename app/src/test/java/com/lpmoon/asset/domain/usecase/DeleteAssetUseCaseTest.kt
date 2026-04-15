package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.repository.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteAssetUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var deleteAssetUseCase: DeleteAssetUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        deleteAssetUseCase = DeleteAssetUseCase(assetRepository)
    }

    @Test
    fun `invoke should delete existing asset and clean histories`() = runTest {
        // Given
        val assetToDelete = Asset(id = 2L, name = "DeleteMe", value = "500", currency = "CNY", type = "Cash")
        val otherAsset = Asset(id = 1L, name = "KeepMe", value = "1000", currency = "USD", type = "Stock")
        val existingAssets = listOf(otherAsset, assetToDelete)
        coEvery { assetRepository.getAllAssets() } returns flowOf(existingAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.deleteHistoriesByAssetId(any()) } coAnswers { }

        // When
        deleteAssetUseCase.invoke(2L)

        // Then
        coVerify {
            assetRepository.saveAssets(withArg { updatedAssets ->
                assertEquals(1, updatedAssets.size)
                assertEquals(otherAsset, updatedAssets[0])
            })
            assetRepository.deleteHistoriesByAssetId(2L)
        }
    }

    @Test
    fun `invoke should save same list when asset not found`() = runTest {
        // Given
        val existingAssets = listOf(
            Asset(id = 1L, name = "A1", value = "100", currency = "CNY", type = "Cash"),
            Asset(id = 3L, name = "A3", value = "300", currency = "EUR", type = "Bond")
        )
        coEvery { assetRepository.getAllAssets() } returns flowOf(existingAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.deleteHistoriesByAssetId(any()) } coAnswers { }

        // When
        deleteAssetUseCase.invoke(999L) // non-existent

        // Then
        coVerify {
            assetRepository.saveAssets(existingAssets)
            assetRepository.deleteHistoriesByAssetId(999L)
        }
    }

    @Test
    fun `invoke should delete only first matching asset when duplicate ids exist`() = runTest {
        // Given: duplicate IDs (shouldn't happen but defensive)
        val duplicate1 = Asset(id = 1L, name = "First", value = "100", currency = "CNY", type = "Cash")
        val duplicate2 = Asset(id = 1L, name = "Second", value = "200", currency = "USD", type = "Stock")
        val otherAsset = Asset(id = 2L, name = "Other", value = "300", currency = "EUR", type = "Bond")
        val existingAssets = listOf(duplicate1, duplicate2, otherAsset)
        coEvery { assetRepository.getAllAssets() } returns flowOf(existingAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.deleteHistoriesByAssetId(any()) } coAnswers { }

        // When
        deleteAssetUseCase.invoke(1L)

        // Then
        coVerify {
            assetRepository.saveAssets(withArg { updatedAssets ->
                assertEquals(2, updatedAssets.size)
                // First duplicate removed, second duplicate remains, other asset remains
                assertEquals(duplicate2, updatedAssets[0])
                assertEquals(otherAsset, updatedAssets[1])
            })
            assetRepository.deleteHistoriesByAssetId(1L)
        }
    }

    @Test
    fun `invoke should handle empty asset list gracefully`() = runTest {
        // Given
        val emptyAssets = emptyList<Asset>()
        coEvery { assetRepository.getAllAssets() } returns flowOf(emptyAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.deleteHistoriesByAssetId(any()) } coAnswers { }

        // When
        deleteAssetUseCase.invoke(1L)

        // Then
        coVerify {
            assetRepository.saveAssets(emptyAssets)
            assetRepository.deleteHistoriesByAssetId(1L)
        }
    }

    @Test
    fun `invoke should delete asset and keep others unchanged`() = runTest {
        // Given
        val assets = (1L..5L).map { id ->
            Asset(id = id, name = "Asset$id", value = "${id * 100}", currency = "CNY", type = "Cash")
        }
        coEvery { assetRepository.getAllAssets() } returns flowOf(assets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.deleteHistoriesByAssetId(any()) } coAnswers { }

        // When
        deleteAssetUseCase.invoke(3L)

        // Then
        coVerify {
            assetRepository.saveAssets(withArg { updatedAssets ->
                assertEquals(4, updatedAssets.size)
                assert(updatedAssets.all { it.id != 3L })
                assertEquals(listOf(1L, 2L, 4L, 5L), updatedAssets.map { it.id })
            })
            assetRepository.deleteHistoriesByAssetId(3L)
        }
    }
}