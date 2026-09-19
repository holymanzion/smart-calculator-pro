package com.holymanzion.calculator.core

import java.math.BigDecimal

/** Why an expression could not be turned into a number. */
enum class CalcError {
    /** The expression is empty or only whitespace. Not shown to the user as an error. */
    Empty,

    /** Malformed input: a stray operator, a missing operand, an unknown name. */
    Syntax,

    /** Parentheses do not balance. */
    UnbalancedParens,

    /** Division (or modulo) by zero. */
    DivisionByZero,

    /** The operation is undefined for that input, e.g. `ln(-1)` or `asin(2)`. */
    Domain,

    /** The result is not finite, or is too large to represent. */
    Overflow,
}

/** Outcome of evaluating an expression. */
sealed interface CalcResult {
    data class Success(val value: BigDecimal) : CalcResult

    data class Failure(val error: CalcError, val position: Int = -1) : CalcResult
}

/** Thrown internally by the parser and evaluator; converted to [CalcResult.Failure] at the boundary. */
internal class CalcException(
    val error: CalcError,
    val position: Int = -1,
) : Exception(error.name)
