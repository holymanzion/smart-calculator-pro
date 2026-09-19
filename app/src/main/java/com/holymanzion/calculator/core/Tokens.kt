package com.holymanzion.calculator.core

/**
 * Lexical units produced by [Tokenizer].
 *
 * [pos] is the index of the token's first character in the source expression and
 * is what error reporting points at.
 */
internal sealed interface Token {
    val pos: Int

    data class NumberLiteral(val text: String, override val pos: Int) : Token
    data class Operator(val kind: OperatorKind, override val pos: Int) : Token
    data class FunctionName(val name: String, override val pos: Int) : Token
    data class ConstantName(val name: String, override val pos: Int) : Token
    data class LeftParen(override val pos: Int) : Token
    data class RightParen(override val pos: Int) : Token
    data class PercentSign(override val pos: Int) : Token
    data class Bang(override val pos: Int) : Token
    data class Radical(override val pos: Int) : Token
    data class End(override val pos: Int) : Token
}

internal enum class OperatorKind(val symbol: Char) {
    Plus('+'),
    Minus('-'),
    Times('*'),
    Divide('/'),
    Power('^'),
}

/** True when a token may begin an operand, which is what licenses implicit multiplication. */
internal fun Token.startsOperand(): Boolean = when (this) {
    is Token.NumberLiteral,
    is Token.ConstantName,
    is Token.FunctionName,
    is Token.LeftParen,
    is Token.Radical,
    -> true

    else -> false
}
