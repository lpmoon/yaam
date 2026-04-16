package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.asset.AssetHistory
import com.lpmoon.asset.domain.model.asset.OperationType
import com.lpmoon.asset.domain.repository.asset.AssetRepository
import com.lpmoon.asset.domain.usecase.asset.GetAssetHistoryUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAssetHistoryUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var getAssetHistoryUseCase: GetAssetHistoryUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        getAssetHistoryUseCase = GetAssetHistoryUseCase(assetRepository)
    }

    @Test
    fun `invoke should return flow of asset histories for given asset id`() = runTest {
        // Given
        val assetId = 1L
        val histories = listOf(
            AssetHistory(id = 100L, assetId = assetId, oldValue = "", newValue = "1000", operationType = OperationType.CREATE),
            AssetHistory(id = 200L, assetId = assetId, oldValue = "1000", newValue = "1500", operationType = OperationType.UPDATE)
        )
        every { assetRepository.getAssetHistory(assetId) } returns flowOf(histories)

        // When
        val resultFlow = getAssetHistoryUseCase.invoke(assetId)
        val result = resultFlow.toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(histories, result[0])
    }

    @Test
    fun `invoke should handle empty history list`() = runTest {
        // Given
        val assetId = 999L
        val emptyHistories = emptyList<AssetHistory>()
        every { assetRepository.getAssetHistory(assetId) } returns flowOf(emptyHistories)

        // When
        val resultFlow = getAssetHistoryUseCase.invoke(assetId)
        val result = resultFlow.toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyHistories, result[0])
    }

    @Test
    fun `invoke should propagate changes from repository`() = runTest {
        // Given
        val assetId = 1L
        val firstHistories = listOf(
            AssetHistory(id = 100L, assetId = assetId, oldValue = "", newValue = "1000", operationType = OperationType.CREATE)
        )
        val secondHistories = listOf(
            AssetHistory(id = 100L, assetId = assetId, oldValue = "", newValue = "1000", operationType = OperationType.CREATE),
            AssetHistory(id = 200L, assetId = assetId, oldValue = "1000", newValue = "1500", operationType = OperationType.UPDATE)
        )

        val flow = kotlinx.coroutines.flow.flow {
            emit(firstHistories)
            emit(secondHistories)
        }
        every { assetRepository.getAssetHistory(assetId) } returns flow

        // When
        val resultFlow = getAssetHistoryUseCase.invoke(assetId)
        val result = resultFlow.toList()

        // Then
        assertEquals(2, result.size)
        assertEquals(firstHistories, result[0])
        assertEquals(secondHistories, result[1])
    }

    @Test
    fun `invoke should return histories filtered by asset id`() = runTest {
        // Given
        val assetId = 2L
        val histories = listOf(
            AssetHistory(id = 100L, assetId = 1L, oldValue = "", newValue = "500", operationType = OperationType.CREATE),
            AssetHistory(id = 200L, assetId = 2L, oldValue = "", newValue = "1000", operationType = OperationType.CREATE),
            AssetHistory(id = 300L, assetId = 2L, oldValue = "1000", newValue = "1200", operationType = OperationType.UPDATE)
        )
        every { assetRepository.getAssetHistory(assetId) } returns flowOf(histories.filter { it.assetId == assetId })

        // When
        val resultFlow = getAssetHistoryUseCase.invoke(assetId)
        val result = resultFlow.toList()

        // Then
        assertEquals(1, result.size)
        val filtered = histories.filter { it.assetId == assetId }
        assertEquals(filtered, result[0])
    }
}