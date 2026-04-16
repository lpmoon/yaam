package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.domain.usecase.asset.FileImportAssetsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FileImportAssetsUseCaseTest {

    private val useCase = FileImportAssetsUseCase()

    @Test
    fun `invoke should import valid json successfully`() = runTest {
        // Given
        val json = """
            [
                {"name": "Asset1", "value": "1000", "currency": "CNY", "type": "Cash"},
                {"name": "Asset2", "value": "2000", "currency": "USD", "type": "Stock"}
            ]
        """.trimIndent()
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo(fileName = "test.json")
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertNull(result.errorMessage)
        assertNotNull(result.importedAssets)
        assertEquals(2, result.importedAssets!!.size)

        val asset1 = result.importedAssets!![0]
        assertEquals(0L, asset1.id) // ID should be 0 (to be assigned by repository)
        assertEquals("Asset1", asset1.name)
        assertEquals("1000", asset1.value)
        assertEquals("CNY", asset1.currency)
        assertEquals("Cash", asset1.type)

        val asset2 = result.importedAssets!![1]
        assertEquals("Asset2", asset2.name)
        assertEquals("2000", asset2.value)
        assertEquals("USD", asset2.currency)
        assertEquals("Stock", asset2.type)
    }

    @Test
    fun `invoke should handle missing currency and type fields`() = runTest {
        // Given: JSON missing currency and type (should use defaults)
        val json = """
            [
                {"name": "Asset1", "value": "1000"}
            ]
        """.trimIndent()
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        val asset = result.importedAssets!![0]
        assertEquals("Asset1", asset.name)
        assertEquals("1000", asset.value)
        assertEquals("CNY", asset.currency) // default
        assertEquals("OTHER", asset.type) // default
    }

    @Test
    fun `invoke should return error for empty json array`() = runTest {
        // Given
        val json = "[]"
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(!result.success)
        assertEquals("导入数据为空或格式不正确", result.errorMessage)
        assertNull(result.importedAssets)
    }

    @Test
    fun `invoke should return error for null json`() = runTest {
        // Given: Gson.fromJson returns null for malformed JSON
        val json = "null"
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(!result.success)
        assertEquals("导入数据为空或格式不正确", result.errorMessage)
        assertNull(result.importedAssets)
    }

    @Test
    fun `invoke should return error for invalid json format`() = runTest {
        // Given
        val json = "not a json"
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(!result.success)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.startsWith("导入失败:"))
        assertNull(result.importedAssets)
    }

    @Test
    fun `invoke should handle json with extra fields`() = runTest {
        // Given: JSON with extra fields (should be ignored by Gson)
        val json = """
            [
                {"name": "Asset1", "value": "1000", "currency": "CNY", "type": "Cash", "extra": "ignored"}
            ]
        """.trimIndent()
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertEquals(1, result.importedAssets!!.size)
    }

    @Test
    fun `invoke should convert empty currency string to default`() = runTest {
        // Given
        val json = """
            [
                {"name": "Asset1", "value": "1000", "currency": "", "type": "Cash"}
            ]
        """.trimIndent()
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertEquals("CNY", result.importedAssets!![0].currency)
    }

    @Test
    fun `invoke should convert empty type string to default`() = runTest {
        // Given
        val json = """
            [
                {"name": "Asset1", "value": "1000", "currency": "CNY", "type": ""}
            ]
        """.trimIndent()
        val params = FileImportAssetsUseCase.Params(
            importData = json,
            importInfo = FileImportAssetsUseCase.ImportInfo()
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertEquals("OTHER", result.importedAssets!![0].type)
    }
}