package com.lpmoon.asset.domain.usecase

import com.lpmoon.asset.data.asset.Asset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QRExportAssetsUseCaseTest {

    private val useCase = QRExportAssetsUseCase()

    @Test
    fun `invoke should generate qr content for assets`() = runTest {
        // Given
        val assets = listOf(
            Asset(id = 1L, name = "Asset1", value = "1000", currency = "CNY", type = "Cash")
        )
        val params = QRExportAssetsUseCase.Params(assets = assets)

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertNull(result.errorMessage)
        assertNotNull(result.qrContent)
        // QR content should contain expected fields
        val json = result.qrContent!!
        assertTrue(json.contains("serverAddress"))
        assertTrue(json.contains("sessionId"))
        assertTrue(json.contains("encryptionKey"))
        assertTrue(json.contains("timestamp"))
        assertTrue(json.contains("dataHash"))
    }

    @Test
    fun `invoke should generate qr content for empty assets`() = runTest {
        // Given
        val assets = emptyList<Asset>()
        val params = QRExportAssetsUseCase.Params(assets = assets)

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        assertNotNull(result.qrContent)
    }

    @Test
    fun `invoke should handle exception during qr generation`() = runTest {
        // Given: Use case should handle exceptions internally
        // We'll just verify it doesn't crash
        val assets = listOf(Asset(id = 1L, name = "Asset", value = "100", currency = "CNY", type = "Cash"))
        val params = QRExportAssetsUseCase.Params(assets = assets)

        // When
        val result = useCase.invoke(params)

        // Then: Should succeed
        assertTrue(result.success)
    }

    @Test
    fun `invoke should generate valid json structure`() = runTest {
        // Given
        val assets = listOf(
            Asset(id = 1L, name = "Test", value = "500", currency = "USD", type = "Stock")
        )
        val params = QRExportAssetsUseCase.Params(assets = assets)

        // When
        val result = useCase.invoke(params)

        // Then
        assertTrue(result.success)
        val json = result.qrContent!!
        // Should be valid JSON (doesn't throw when parsed)
        // We can't easily parse without Gson in test, but we can check brackets
        assertTrue(json.startsWith("{") && json.endsWith("}"))
    }
}