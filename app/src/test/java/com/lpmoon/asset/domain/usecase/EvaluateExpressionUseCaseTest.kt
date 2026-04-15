package com.lpmoon.asset.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluateExpressionUseCaseTest {

    private val useCase = EvaluateExpressionUseCase()

    @Test
    fun `invoke should evaluate simple addition`() = runTest {
        // Given
        val expression = "100+50"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(150.0, result, 0.001)
    }

    @Test
    fun `invoke should evaluate multiplication`() = runTest {
        // Given
        val expression = "100*2"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(200.0, result, 0.001)
    }

    @Test
    fun `invoke should evaluate division`() = runTest {
        // Given
        val expression = "100/4"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(25.0, result, 0.001)
    }

    @Test
    fun `invoke should evaluate subtraction`() = runTest {
        // Given
        val expression = "100-30"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(70.0, result, 0.001)
    }

    @Test
    fun `invoke should evaluate complex expression`() = runTest {
        // Given
        val expression = "(100+50)*2"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(300.0, result, 0.001)
    }

    @Test
    fun `invoke should evaluate decimal numbers`() = runTest {
        // Given
        val expression = "100.5+50.2"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(150.7, result, 0.001)
    }

    @Test
    fun `invoke should evaluate negative numbers`() = runTest {
        // Given
        val expression = "-100+50"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(-50.0, result, 0.001)
    }

    @Test
    fun `invoke should handle single number`() = runTest {
        // Given
        val expression = "100"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `invoke should handle empty expression`() = runTest {
        // Given
        val expression = ""

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `invoke should handle expression with spaces`() = runTest {
        // Given
        val expression = "100 + 50"

        // When
        val result = useCase.invoke(expression)

        // Then
        assertEquals(150.0, result, 0.001)
    }
}