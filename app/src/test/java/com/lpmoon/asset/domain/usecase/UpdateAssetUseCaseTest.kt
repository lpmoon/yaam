package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.model.asset.OperationType
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.asset.UpdateAssetUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateAssetUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var updateAssetUseCase: UpdateAssetUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        updateAssetUseCase = UpdateAssetUseCase(assetRepository)
    }

    @Test
    fun `invoke should update existing asset and record history`() = runTest {
        // Given
        val existingAsset = Asset(
            id = 1L,
            name = "Old Name",
            value = "1000",
            currency = "CNY",
            type = "Cash"
        )
        val existingAssets = listOf(existingAsset)
        coEvery { assetRepository.getAllAssets() } returns flowOf(existingAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.addAssetHistory(any()) } coAnswers { }

        val params = UpdateAssetUseCase.Params(
            assetId = 1L,
            name = "New Name",
            value = "1500",
            currency = "USD",
            type = "Stock"
        )

        // When
        updateAssetUseCase.invoke(params)

        // Then
        coVerify {
            assetRepository.saveAssets(withArg { updatedAssets ->
                assertEquals(1, updatedAssets.size)
                val updatedAsset = updatedAssets[0]
                assertEquals(1L, updatedAsset.id)
                assertEquals("New Name", updatedAsset.name)
                assertEquals("1500", updatedAsset.value)
                assertEquals("USD", updatedAsset.currency)
                assertEquals("Stock", updatedAsset.type)
            })
            assetRepository.addAssetHistory(withArg { history ->
                assertEquals(1L, history.assetId)
                assertEquals("1000", history.oldValue)
                assertEquals("1500", history.newValue)
                assertEquals(OperationType.UPDATE, history.operationType)
            })
        }
    }

    @Test
    fun `invoke should do nothing when asset not found`() = runTest {
        // Given
        val existingAssets = listOf(
            Asset(id = 1L, name = "A1", value = "100", currency = "CNY", type = "Cash"),
            Asset(id = 2L, name = "A2", value = "200", currency = "USD", type = "Stock")
        )
        coEvery { assetRepository.getAllAssets() } returns flowOf(existingAssets)
        // saveAssets and addAssetHistory should not be called

        val params = UpdateAssetUseCase.Params(
            assetId = 999L, // non-existent
            name = "New Name",
            value = "300",
            currency = "EUR",
            type = "Bond"
        )

        // When
        updateAssetUseCase.invoke(params)

        // Then
        coVerify(exactly = 0) {
            assetRepository.saveAssets(any())
            assetRepository.addAssetHistory(any())
        }
    }

    @Test
    fun `invoke should update only first matching asset when duplicate ids exist`() = runTest {
        // Given: duplicate IDs (shouldn't happen but defensive)
        val duplicateAsset1 = Asset(id = 1L, name = "First", value = "100", currency = "CNY", type = "Cash")
        val duplicateAsset2 = Asset(id = 1L, name = "Second", value = "200", currency = "USD", type = "Stock")
        val existingAssets = listOf(duplicateAsset1, duplicateAsset2)
        coEvery { assetRepository.getAllAssets() } returns flowOf(existingAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.addAssetHistory(any()) } coAnswers { }

        val params = UpdateAssetUseCase.Params(
            assetId = 1L,
            name = "Updated",
            value = "300",
            currency = "EUR",
            type = "Bond"
        )

        // When
        updateAssetUseCase.invoke(params)

        // Then: only first duplicate should be updated
        coVerify {
            assetRepository.saveAssets(withArg { updatedAssets ->
                assertEquals(2, updatedAssets.size)
                // First asset updated
                assertEquals("Updated", updatedAssets[0].name)
                assertEquals("300", updatedAssets[0].value)
                assertEquals("EUR", updatedAssets[0].currency)
                assertEquals("Bond", updatedAssets[0].type)
                // Second asset unchanged
                assertEquals("Second", updatedAssets[1].name)
                assertEquals("200", updatedAssets[1].value)
                assertEquals("USD", updatedAssets[1].currency)
                assertEquals("Stock", updatedAssets[1].type)
            })
            // History should be recorded once
            assetRepository.addAssetHistory(withArg { history ->
                assertEquals(1L, history.assetId)
                assertEquals("100", history.oldValue) // old value from first duplicate
                assertEquals("300", history.newValue)
            })
        }
    }

    @Test
    fun `invoke should record history with correct timestamp`() = runTest {
        // Given
        val existingAsset = Asset(id = 1L, name = "Asset", value = "500", currency = "CNY", type = "Cash")
        coEvery { assetRepository.getAllAssets() } returns flowOf(listOf(existingAsset))
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.addAssetHistory(any()) } coAnswers { }

        val params = UpdateAssetUseCase.Params(
            assetId = 1L,
            name = "Updated",
            value = "600",
            currency = "CNY",
            type = "Cash"
        )

        // When
        updateAssetUseCase.invoke(params)

        // Then
        coVerify {
            assetRepository.addAssetHistory(withArg { history ->
                assert(history.id > 0) // timestamp
                assertEquals(OperationType.UPDATE, history.operationType)
            })
        }
    }
}