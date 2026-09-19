package com.holymanzion.calculator.core

/**
 * Turns an expression string into tokens.
 *
 * The keypad emits typographic characters (`×`, `÷`, `−`, `π`, `√`) while tests and
 * pasted text tend to use ASCII, so both spellings are accepted.
 *
 * Note the deliberate case split on `e`: uppercase `E` is the scientific-notation
 * exponent marker inside a number (`1.5E3`), lowercase `e` is Euler's constant.
 */
internal object Tokenizer {

    private val functionNames = setOf(
        "sin", "cos", "tan",
        "asin", "acos", "atan",
        "sinh", "cosh", "tanh",
        "ln", "log", "log2",
        "sqrt", "cbrt", "abs", "exp",
        "floor", "ceil", "round",
    )

    private val constantNames = setOf("pi", "e", "ans")

    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < input.length) {
            val c = input[i]

            when {
                c.isWhitespace() -> i++

                c.isDigit() || c == '.' -> {
                    val start = i
                    i = scanNumber(input, i)
                    tokens += Token.NumberLiteral(input.substring(start, i), start)
                }

                // `π` is a Unicode letter, so it has to be claimed before the
                // identifier branch or it is scanned as an unknown name.
                c == 'π' -> {
                    tokens += Token.ConstantName("pi", i)
                    i++
                }

                c.isLetter() -> {
                    val start = i
                    while (i < input.length && input[i].isLetterOrDigit() && input[i] != 'π') i++
                    val word = input.substring(start, i)
                    val lower = word.lowercase()
                    tokens += when {
                        lower in functionNames -> Token.FunctionName(lower, start)
                        lower in constantNames -> Token.ConstantName(lower, start)
                        else -> throw CalcException(CalcError.Syntax, start)
                    }
                }

                else -> {
                    tokens += singleCharToken(c, i) ?: throw CalcException(CalcError.Syntax, i)
                    i++
                }
            }
        }

        tokens += Token.End(input.length)
        return tokens
    }

    /**
     * Consumes `digits [. digits] [E [+|-] digits]` starting at [start].
     *
     * The exponent is only absorbed when at least one digit follows it, so a trailing
     * `E` is left for the parser to reject rather than silently swallowed.
     */
    private fun scanNumber(input: String, start: Int): Int {
        var i = start
        var seenDot = false

        while (i < input.length) {
            val c = input[i]
            if (c.isDigit()) {
                i++
            } else if (c == '.' && !seenDot) {
                seenDot = true
                i++
            } else {
                break
            }
        }

        if (i < input.length && input[i] == 'E') {
            var j = i + 1
            if (j < input.length && (input[j] == '+' || input[j] == '-' || input[j] == '−')) j++
            if (j < input.length && input[j].isDigit()) {
                while (j < input.length && input[j].isDigit()) j++
                i = j
            }
        }

        return i
    }

    private fun singleCharToken(c: Char, pos: Int): Token? = when (c) {
        '+' -> Token.Operator(OperatorKind.Plus, pos)
        '-', '−' -> Token.Operator(OperatorKind.Minus, pos)
        '*', '×', '∙', '⋅' -> Token.Operator(OperatorKind.Times, pos)
        '/', '÷' -> Token.Operator(OperatorKind.Divide, pos)
        '^' -> Token.Operator(OperatorKind.Power, pos)
        '(' -> Token.LeftParen(pos)
        ')' -> Token.RightParen(pos)
        '%' -> Token.PercentSign(pos)
        '!' -> Token.Bang(pos)
        '√' -> Token.Radical(pos)
        'π' -> Token.ConstantName("pi", pos)
        else -> null
    }
}
