package com.lpmoon.asset

import org.junit.Test
import com.lpmoon.asset.util.ExpressionEvaluator
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testBeautifyExpression() {
        assertEquals("100 + 50", ExpressionEvaluator.beautify("100+50"))
        assertEquals("100 * 20", ExpressionEvaluator.beautify("100*20"))
        assertEquals("100 + 50 * 2", ExpressionEvaluator.beautify("100+50*2"))
        assertEquals("100 - 30", ExpressionEvaluator.beautify("100-30"))
        assertEquals("100 / 25", ExpressionEvaluator.beautify("100/25"))
        assertEquals("100", ExpressionEvaluator.beautify("100"))
        assertEquals("-100 + 50", ExpressionEvaluator.beautify("-100+50"))
        assertEquals("(100 + 50) * 2", ExpressionEvaluator.beautify("(100+50)*2"))
    }
}