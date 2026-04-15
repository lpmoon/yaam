package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.Asset
import com.lpmoon.asset.domain.repository.AssetRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAllAssetsUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var getAllAssetsUseCase: GetAllAssetsUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        getAllAssetsUseCase = GetAllAssetsUseCase(assetRepository)
    }

    @Test
    fun `invoke should return flow of assets from repository`() = runTest {
        // Given
        val expectedAssets = listOf(
            Asset(id = 1, name = "Asset1", value = "1000", currency = "CNY", type = "Cash"),
            Asset(id = 2, name = "Asset2", value = "2000", currency = "USD", type = "Stock")
        )
        every { assetRepository.getAllAssets() } returns flowOf(expectedAssets)

        // When
        val resultFlow = getAllAssetsUseCase.invoke()
        val result = resultFlow.toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedAssets, result[0])
    }

    @Test
    fun `invoke should return empty flow when repository returns empty`() = runTest {
        // Given
        val emptyAssets = emptyList<Asset>()
        every { assetRepository.getAllAssets() } returns flowOf(emptyAssets)

        // When
        val resultFlow = getAllAssetsUseCase.invoke()
        val result = resultFlow.toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyAssets, result[0])
    }

    @Test
    fun `invoke should propagate changes from repository`() = runTest {
        // Given
        val firstAssets = listOf(Asset(id = 1, name = "A1", value = "100", currency = "CNY", type = "Cash"))
        val secondAssets = listOf(
            Asset(id = 1, name = "A1", value = "100", currency = "CNY", type = "Cash"),
            Asset(id = 2, name = "A2", value = "200", currency = "USD", type = "Stock")
        )

        // Simulate flow that emits multiple values (though repository typically emits single list)
        val flow = kotlinx.coroutines.flow.flow {
            emit(firstAssets)
            emit(secondAssets)
        }
        every { assetRepository.getAllAssets() } returns flow

        // When
        val resultFlow = getAllAssetsUseCase.invoke()
        val result = resultFlow.toList()

        // Then
        assertEquals(2, result.size)
        assertEquals(firstAssets, result[0])
        assertEquals(secondAssets, result[1])
    }
}