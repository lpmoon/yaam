package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.model.AssetHistory
import com.lpmoon.asset.domain.model.OperationType
import com.lpmoon.asset.domain.repository.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddAssetUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var addAssetUseCase: AddAssetUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        addAssetUseCase = AddAssetUseCase(assetRepository)
    }

    @Test
    fun `invoke should add asset with id 1 when no existing assets`() = runTest {
        // Given
        val emptyAssets = emptyList<Asset>()
        coEvery { assetRepository.getAllAssets() } returns flowOf(emptyAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.addAssetHistory(any()) } coAnswers { }

        val params = AddAssetUseCase.Params(
            name = "Test Asset",
            value = "1000",
            currency = "CNY",
            type = "Cash"
        )

        // When
        addAssetUseCase.invoke(params)

        // Then
        coVerify {
            assetRepository.saveAssets(withArg { assets ->
                assertEquals(1, assets.size)
                val asset = assets[0]
                assertEquals(1L, asset.id)
                assertEquals("Test Asset", asset.name)
                assertEquals("1000", asset.value)
                assertEquals("CNY", asset.currency)
                assertEquals("Cash", asset.type)
            })
            assetRepository.addAssetHistory(withArg { history ->
                assertEquals(OperationType.CREATE, history.operationType)
                assertEquals("", history.oldValue)
                assertEquals("1000", history.newValue)
            })
        }
    }

    @Test
    fun `invoke should add asset with incremented id when existing assets present`() = runTest {
        // Given
        val existingAssets = listOf(
            Asset(id = 1, name = "Asset1", value = "500", currency = "CNY", type = "Cash"),
            Asset(id = 3, name = "Asset3", value = "200", currency = "USD", type = "Stock")
        )
        coEvery { assetRepository.getAllAssets() } returns flowOf(existingAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.addAssetHistory(any()) } coAnswers { }

        val params = AddAssetUseCase.Params(
            name = "New Asset",
            value = "1500",
            currency = "EUR",
            type = "Bond"
        )

        // When
        addAssetUseCase.invoke(params)

        // Then
        coVerify {
            assetRepository.saveAssets(withArg { assets ->
                assertEquals(3, assets.size)
                val newAsset = assets.find { it.id == 4L } // 3 + 1 = 4
                assertEquals("New Asset", newAsset?.name)
                assertEquals("1500", newAsset?.value)
                assertEquals("EUR", newAsset?.currency)
                assertEquals("Bond", newAsset?.type)
            })
            assetRepository.addAssetHistory(withArg { history ->
                assertEquals(4L, history.assetId)
            })
        }
    }

    @Test
    fun `invoke should record asset history with correct timestamp`() = runTest {
        // Given
        val emptyAssets = emptyList<Asset>()
        coEvery { assetRepository.getAllAssets() } returns flowOf(emptyAssets)
        coEvery { assetRepository.saveAssets(any()) } coAnswers { }
        coEvery { assetRepository.addAssetHistory(any()) } coAnswers { }

        val params = AddAssetUseCase.Params(
            name = "Test",
            value = "100",
            currency = "CNY",
            type = "Cash"
        )

        // When
        addAssetUseCase.invoke(params)

        // Then
        coVerify {
            assetRepository.addAssetHistory(withArg { history ->
                assertEquals(OperationType.CREATE, history.operationType)
                assert(history.id > 0) // timestamp should be positive
            })
        }
    }
}