package com.lpmoon.asset.domain.usecase.asset

import com.lpmoon.asset.domain.usecase.UseCase
import com.lpmoon.asset.util.ExpressionEvaluator


/**
 * 表达式求值用例
 */
class EvaluateExpressionUseCase() : UseCase<String, Double> {

    override suspend fun invoke(expression: String): Double {
        return ExpressionEvaluator.evaluate(expression)
    }
}