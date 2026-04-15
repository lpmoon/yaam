package com.lpmoon.asset.util

/**
 * 表达式求值器
 * 支持四则运算：+ - * /
 * 示例：
 * - "100" → 100.0
 * - "100+50" → 150.0
 * - "100*20" → 2000.0
 * - "1000/10" → 100.0
 * - "100+50*2" → 200.0 (遵循运算优先级)
 */
object ExpressionEvaluator {

    fun evaluate(expression: String): Double {
        if (expression.isBlank()) return 0.0

        try {
            // 移除所有空格
            val trimmedExpression = expression.replace(" ", "")

            // 如果是纯数字，直接返回
            if (trimmedExpression.matches(Regex("^-?\\d+(\\.\\d+)?$"))) {
                return trimmedExpression.toDouble()
            }

            // 解析并计算表达式
            val tokens = tokenize(trimmedExpression)
            val rpn = toRPN(tokens)
            return evaluateRPN(rpn)
        } catch (e: Exception) {
            return 0.0
        }
    }

    /**
     * 将表达式转换为 Token 列表
     */
    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0

        while (i < expression.length) {
            val c = expression[i]

            when {
                c.isDigit() || c == '.' || (c == '-' && (i == 0 || expression[i - 1] == '(')) -> {
                    // 数字（包括负数和小数）
                    val numStart = i
                    // 如果是负号，先跳过负号
                    if (c == '-') {
                        i++
                    }
                    // 收集数字和小数点
                    while (i < expression.length &&
                        (expression[i].isDigit() || expression[i] == '.')) {
                        i++
                    }
                    tokens.add(expression.substring(numStart, i))
                }
                c in "+-*/()" -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> {
                    // 跳过其他字符
                    i++
                }
            }
        }

        return tokens
    }

    /**
     * 将中缀表达式转换为后缀表达式（逆波兰表达式）
     * 使用 Shunting-yard 算法
     */
    private fun toRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = mutableListOf<String>()

        val precedence = mapOf(
            "+" to 1,
            "-" to 1,
            "*" to 2,
            "/" to 2
        )

        for (token in tokens) {
            when {
                token.matches(Regex("^-?\\d+(\\.\\d+)?$")) -> {
                    // 数字，直接输出
                    output.add(token)
                }
                token == "(" -> {
                    operators.add(token)
                }
                token == ")" -> {
                    // 弹出操作符直到遇到左括号
                    while (operators.isNotEmpty() && operators[operators.size - 1] != "(") {
                        output.add(operators.removeAt(operators.size - 1))
                    }
                    if (operators.isNotEmpty()) {
                        operators.removeAt(operators.size - 1) // 移除左括号
                    }
                }
                token in precedence.keys -> {
                    // 操作符，根据优先级处理
                    while (operators.isNotEmpty() &&
                        operators[operators.size - 1] != "(" &&
                        precedence[operators[operators.size - 1]]!! >= precedence[token]!!) {
                        output.add(operators.removeAt(operators.size - 1))
                    }
                    operators.add(token)
                }
            }
        }

        // 弹出剩余的操作符
        while (operators.isNotEmpty()) {
            output.add(operators.removeAt(operators.size - 1))
        }

        return output
    }

    /**
     * 计算后缀表达式
     */
    private fun evaluateRPN(rpn: List<String>): Double {
        val rpnStack = mutableListOf<Double>()

        for (token in rpn) {
            when {
                token.matches(Regex("^-?\\d+(\\.\\d+)?$")) -> {
                    rpnStack.add(token.toDouble())
                }
                token in setOf("+", "-", "*", "/") -> {
                    if (rpnStack.size < 2) return 0.0

                    val b = rpnStack.removeAt(rpnStack.size - 1)
                    val a = rpnStack.removeAt(rpnStack.size - 1)

                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> {
                            if (b == 0.0) 0.0 else a / b
                        }
                        else -> 0.0
                    }
                    rpnStack.add(result)
                }
            }
        }

        return rpnStack.firstOrNull() ?: 0.0
    }

    /**
     * 判断字符串是否是有效的表达式
     */
    fun isValidExpression(expression: String): Boolean {
        if (expression.isBlank()) return false

        try {
            val trimmed = expression.replace(" ", "")
            if (trimmed.matches(Regex("^-?\\d+(\\.\\d+)?$"))) return true

            val tokens = tokenize(trimmed)
            val rpn = toRPN(tokens)
            evaluateRPN(rpn)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 美化表达式，在操作符周围添加空格
     * 示例：
     * - "100+50" → "100 + 50"
     * - "100*20" → "100 * 20"
     * - "100+50*2" → "100 + 50 * 2"
     */
    fun beautify(expression: String): String {
        if (expression.isBlank()) return expression

        // 移除现有空格
        val trimmed = expression.replace(" ", "")

        // 使用正则表达式在操作符周围添加空格，但要小心负号
        // 匹配操作符 + - * /，但负号如果是表达式开头或前面是操作符或左括号，则不是二元操作符
        // 简化处理：在操作符前后添加空格，然后合并多余空格
        var result = trimmed.replace(Regex("(?<=[0-9)])([+\\-*/])(?=[0-9(])")) { match ->
            " ${match.value} "
        }

        // 处理负号：如果负号在开头或前面是操作符或左括号，则不要在前面加空格
        // 但我们已经添加了空格，需要修复
        // 简化：移除开头负号前的空格
        if (result.startsWith("- ")) {
            result = "-" + result.substring(2)
        }
        // 移除括号内的负号前的空格
        result = result.replace(Regex("\\(\\s*-"), "(-")

        // 合并多个空格
        result = result.replace(Regex("\\s+"), " ").trim()

        return result
    }
}
