package com.lpmoon.asset.domain.usecase

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lpmoon.asset.domain.usecase.asset.QRImportAssetsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QRImportAssetsUseCaseTest {

    private lateinit var context: Context
    private lateinit var useCase: QRImportAssetsUseCase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        useCase = QRImportAssetsUseCase()
    }

    @Test
    fun invoke_should_return_error_for_invalid_qr_content() = runTest {
        // Given: Invalid QR content (not JSON)
        val params = QRImportAssetsUseCase.Params(
            context = context,
            qrContent = "not a valid json"
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertFalse(result.success)
        assertNotNull(result.errorMessage)
        // 可能返回"导入失败: ..." 或 "无法识别二维码格式..."
        assertTrue(
            result.errorMessage?.startsWith("导入失败") == true ||
            result.errorMessage == "无法识别二维码格式。请确保扫描的是资产同步二维码。"
        )
        assertNull(result.importedAssets)
    }

    @Test
    fun invoke_should_import_assets_from_direct_export_asset_json() = runTest {
        // Given: Direct export asset JSON format
        val json = """
            [
                {"name": "Asset1", "value": "1000", "currency": "CNY", "type": "Cash"},
                {"name": "Asset2", "value": "2000", "currency": "USD", "type": "Stock"}
            ]
        """.trimIndent()
        val params = QRImportAssetsUseCase.Params(
            context = context,
            qrContent = json
        )

        // When
        val result = useCase.invoke(params)

        // Then: Should succeed because it's valid export asset JSON
        assertTrue(result.success)
        assertNotNull(result.importedAssets)
        assertEquals(2, result.importedAssets!!.size)
    }

    @Test
    fun invoke_should_import_assets_from_simple_map_json() = runTest {
        // Given: Simple map format (missing some fields)
        val json = """
            [
                {"name": "Cash", "value": "500"},
                {"name": "Stock", "value": "1000", "currency": "USD"}
            ]
        """.trimIndent()
        val params = QRImportAssetsUseCase.Params(
            context = context,
            qrContent = json
        )

        // When
        val result = useCase.invoke(params)

        // Then: Should succeed and fill defaults
        assertTrue(result.success)
        assertNotNull(result.importedAssets)
        assertEquals(2, result.importedAssets!!.size)
        assertEquals("CNY", result.importedAssets!![0].currency)
        assertEquals("OTHER", result.importedAssets!![0].type)
    }

    @Test
    fun invoke_should_return_error_for_empty_json_array() = runTest {
        // Given
        val json = "[]"
        val params = QRImportAssetsUseCase.Params(
            context = context,
            qrContent = json
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertFalse(result.success)
        assertNotNull(result.errorMessage)
        // 可能是"无法识别二维码格式..." 或 "导入失败: ..."
        assertTrue(
            result.errorMessage == "无法识别二维码格式。请确保扫描的是资产同步二维码。" ||
            result.errorMessage?.startsWith("导入失败") == true
        )
    }

    @Test
    fun invoke_should_return_error_for_null_json() = runTest {
        // Given
        val json = "null"
        val params = QRImportAssetsUseCase.Params(
            context = context,
            qrContent = json
        )

        // When
        val result = useCase.invoke(params)

        // Then
        assertFalse(result.success)
        assertNotNull(result.errorMessage)
        // 可能是"无法识别二维码格式..." 或 "导入失败: ..."
        assertTrue(
            result.errorMessage == "无法识别二维码格式。请确保扫描的是资产同步二维码。" ||
            result.errorMessage?.startsWith("导入失败") == true
        )
    }

}