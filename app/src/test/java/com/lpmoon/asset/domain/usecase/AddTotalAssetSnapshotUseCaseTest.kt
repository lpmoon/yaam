package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.TotalAssetSnapshot
import com.lpmoon.asset.domain.repository.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddTotalAssetSnapshotUseCaseTest {

    private lateinit var assetRepository: AssetRepository
    private lateinit var addTotalAssetSnapshotUseCase: AddTotalAssetSnapshotUseCase

    @Before
    fun setUp() {
        assetRepository = mockk()
        addTotalAssetSnapshotUseCase = AddTotalAssetSnapshotUseCase(assetRepository)
    }

    @Test
    fun `invoke should create snapshot with current timestamp and call repository`() = runTest {
        // Given
        coEvery { assetRepository.addTotalAssetSnapshot(any()) } coAnswers { }
        val totalValue = 5000.0

        // When
        addTotalAssetSnapshotUseCase.invoke(totalValue)

        // Then
        coVerify {
            assetRepository.addTotalAssetSnapshot(withArg { snapshot ->
                assertTrue(snapshot.totalValue == totalValue)
                assertTrue(snapshot.timestamp > 0)
            })
        }
    }

    @Test
    fun `invoke should propagate exception when repository fails`() = runTest {
        // Given
        val expectedException = RuntimeException("Database error")
        coEvery { assetRepository.addTotalAssetSnapshot(any()) } throws expectedException

        // When & Then
        try {
            addTotalAssetSnapshotUseCase.invoke(1000.0)
            throw AssertionError("Expected exception not thrown")
        } catch (e: RuntimeException) {
            org.junit.Assert.assertEquals(expectedException, e)
        }
    }

    @Test
    fun `invoke should handle zero total value`() = runTest {
        // Given
        coEvery { assetRepository.addTotalAssetSnapshot(any()) } coAnswers { }

        // When
        addTotalAssetSnapshotUseCase.invoke(0.0)

        // Then
        coVerify {
            assetRepository.addTotalAssetSnapshot(withArg { snapshot ->
                assertTrue(snapshot.totalValue == 0.0)
            })
        }
    }

    @Test
    fun `invoke should handle negative total value`() = runTest {
        // Given
        coEvery { assetRepository.addTotalAssetSnapshot(any()) } coAnswers { }

        // When
        addTotalAssetSnapshotUseCase.invoke(-1000.0)

        // Then
        coVerify {
            assetRepository.addTotalAssetSnapshot(withArg { snapshot ->
                assertTrue(snapshot.totalValue == -1000.0)
            })
        }
    }
}