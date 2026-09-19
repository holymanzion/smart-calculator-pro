package com.holymanzion.calculator.core

import java.math.BigDecimal

/**
 * Entry point to the calculation engine.
 *
 * Pure Kotlin with no Android dependencies, so the whole engine is exercisable from
 * plain JVM unit tests.
 */
object Calculator {

    /**
     * Evaluates [expression].
     *
     * @param angleMode unit for trigonometric arguments.
     * @param ans value substituted for the `ans` identifier.
     */
    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.Degrees,
        ans: BigDecimal = BigDecimal.ZERO,
    ): CalcResult {
        val source = NumberFormatter.ungroup(expression)
        if (source.isBlank()) return CalcResult.Failure(CalcError.Empty)

        return try {
            val tokens = Tokenizer.tokenize(source)
            val tree = Parser(tokens).parse()
            val value = Evaluator(angleMode, ans).evaluate(tree)

            if (NumberFormatter.isOutOfRange(value)) {
                CalcResult.Failure(CalcError.Overflow)
            } else {
                CalcResult.Success(value)
            }
        } catch (e: CalcException) {
            CalcResult.Failure(e.error, e.position)
        } catch (_: ArithmeticException) {
            // Backstop for anything BigDecimal rejects that the guards above missed.
            CalcResult.Failure(CalcError.Overflow)
        }
    }

    /**
     * Convenience wrapper that returns display text, or null when [expression] does not
     * currently evaluate. Used for the live preview, where a half-typed expression is
     * normal and should simply show nothing.
     */
    fun preview(
        expression: String,
        angleMode: AngleMode = AngleMode.Degrees,
        ans: BigDecimal = BigDecimal.ZERO,
        grouped: Boolean = true,
    ): String? = when (val result = evaluate(expression, angleMode, ans)) {
        is CalcResult.Success -> NumberFormatter.format(result.value, grouped)
        is CalcResult.Failure -> null
    }
}
