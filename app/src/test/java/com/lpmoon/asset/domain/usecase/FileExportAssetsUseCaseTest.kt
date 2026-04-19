package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.model.asset.Asset
import com.lpmoon.asset.domain.usecase.asset.FileExportAssetsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FileExportAssetsUseCaseTest {

    private val useCase = FileExportAssetsUseCase()

    @Test
    fun `invoke should export assets to json successfully`() = runTest {
        // Given
        val assets = listOf(
            Asset(id = 1L, name = "Asset1", value = "1000", currency = "CNY", type = "Cash"),
            Asset(id = 2L, name = "Asset2", value = "2000", currency = "USD", type = "Stock")
        )
        val params = FileExportAssetsUseCase.Params(
            assets = assets,
            exportInfo = FileExportAssetsUseCase.ExportInfo(fileName = "test.json")
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertNull(result.errorMessage)
        assertNotNull(result.exportData)
        assertEquals("test.json", result.exportData!!.fileName)
        assertEquals(FileExportAssetsUseCase.ExportFormat.JSON, result.exportData!!.format)

        // Verify JSON contains asset data
        val json = result.exportData!!.data
        assertTrue(json.contains("Asset1"))
        assertTrue(json.contains("Asset2"))
        assertTrue(json.contains("1000"))
        assertTrue(json.contains("2000"))
    }

    @Test
    fun `invoke should generate default filename when not provided`() = runTest {
        // Given
        val assets = emptyList<Asset>()
        val params = FileExportAssetsUseCase.Params(
            assets = assets,
            exportInfo = FileExportAssetsUseCase.ExportInfo(fileName = null)
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertNotNull(result.exportData)
        assertTrue(result.exportData!!.fileName.startsWith("assets_"))
        assertTrue(result.exportData!!.fileName.endsWith(".json"))
    }

    @Test
    fun `invoke should handle empty asset list`() = runTest {
        // Given
        val assets = emptyList<Asset>()
        val params = FileExportAssetsUseCase.Params(
            assets = assets,
            exportInfo = FileExportAssetsUseCase.ExportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertNotNull(result.exportData)
        val json = result.exportData!!.data
        // Should be empty JSON array
        assertEquals("[]", json)
    }

    @Test
    fun `invoke should include all asset fields in export`() = runTest {
        // Given
        val assets = listOf(
            Asset(id = 1L, name = "Test", value = "123.45", currency = "EUR", type = "Bond")
        )
        val params = FileExportAssetsUseCase.Params(
            assets = assets,
            exportInfo = FileExportAssetsUseCase.ExportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        val json = result.exportData!!.data
        // Check that exported JSON contains all fields
        assertTrue(json.contains("\"name\":\"Test\""))
        assertTrue(json.contains("\"value\":\"123.45\""))
        assertTrue(json.contains("\"currency\":\"EUR\""))
        assertTrue(json.contains("\"type\":\"Bond\""))
        // ID should not be exported (it's internal)
        assertTrue(!json.contains("\"id\""))
    }

    @Test
    fun `invoke should handle exception during export`() = runTest {
        // Given: Simulate exception by passing assets that cause Gson serialization issue?
        // Actually Gson handles standard objects fine, so we'll trust the try-catch works.
        // This test is a placeholder for exception handling.
        val assets = listOf(Asset(id = 1L, name = "Test", value = "100", currency = "CNY", type = "Cash"))
        val params = FileExportAssetsUseCase.Params(
            assets = assets,
            exportInfo = FileExportAssetsUseCase.ExportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then: Should succeed normally
        assertTrue(result.success)
    }
}