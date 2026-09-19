package com.holymanzion.calculator.core

import java.math.BigDecimal

/**
 * Recursive-descent parser.
 *
 * Grammar, loosest binding first:
 * ```
 * expression := term (('+' | '-') term)*
 * term       := unary (('*' | '/') unary | unary)*      // bare `unary` = implicit multiplication
 * unary      := ('+' | '-') unary | '√' unary | power
 * power      := postfix ('^' unary)?                    // right-associative
 * postfix    := primary ('%' | '!')*
 * primary    := NUMBER | CONSTANT | FUNC '(' expression ')' | '(' expression ')'
 * ```
 *
 * Putting `unary` above `power` is what makes `-2^2` evaluate to `-4` (the negation
 * applies to the whole power) while `2^-3` still parses, because `power`'s right side
 * recurses back into `unary`.
 */
internal class Parser(private val tokens: List<Token>) {

    private var index = 0

    private val current: Token get() = tokens[index]

    fun parse(): Node {
        if (current is Token.End) throw CalcException(CalcError.Empty, 0)
        val node = parseExpression()
        val token = current
        if (token !is Token.End) {
            // A leftover ')' is a balance problem; anything else is a plain syntax error.
            val error = if (token is Token.RightParen) CalcError.UnbalancedParens else CalcError.Syntax
            throw CalcException(error, token.pos)
        }
        return node
    }

    private fun parseExpression(): Node {
        var left = parseTerm()
        while (true) {
            val token = current
            if (token is Token.Operator && (token.kind == OperatorKind.Plus || token.kind == OperatorKind.Minus)) {
                index++
                val right = parseTerm()
                left = Node.Binary(token.kind, left, right, token.pos)
            } else {
                return left
            }
        }
    }

    private fun parseTerm(): Node {
        var left = parseUnary()
        while (true) {
            val token = current
            when {
                token is Token.Operator &&
                    (token.kind == OperatorKind.Times || token.kind == OperatorKind.Divide) -> {
                    index++
                    left = Node.Binary(token.kind, left, parseUnary(), token.pos)
                }

                // `2π`, `3(4+5)`, `2sin(1)` — juxtaposition means multiplication.
                token.startsOperand() -> {
                    left = Node.Binary(OperatorKind.Times, left, parseUnary(), token.pos)
                }

                else -> return left
            }
        }
    }

    private fun parseUnary(): Node {
        val token = current
        if (token is Token.Operator) {
            when (token.kind) {
                OperatorKind.Minus -> {
                    index++
                    return Node.Negate(parseUnary())
                }

                OperatorKind.Plus -> {
                    index++
                    return parseUnary()
                }

                else -> throw CalcException(CalcError.Syntax, token.pos)
            }
        }
        if (token is Token.Radical) {
            index++
            return Node.Call("sqrt", parseUnary(), token.pos)
        }
        return parsePower()
    }

    private fun parsePower(): Node {
        val base = parsePostfix()
        val token = current
        if (token is Token.Operator && token.kind == OperatorKind.Power) {
            index++
            return Node.Binary(OperatorKind.Power, base, parseUnary(), token.pos)
        }
        return base
    }

    private fun parsePostfix(): Node {
        var node = parsePrimary()
        while (true) {
            node = when (val token = current) {
                is Token.PercentSign -> {
                    index++
                    Node.PercentOf(node)
                }

                is Token.Bang -> {
                    index++
                    Node.Factorial(node, token.pos)
                }

                else -> return node
            }
        }
    }

    private fun parsePrimary(): Node {
        when (val token = current) {
            is Token.NumberLiteral -> {
                index++
                return Node.Literal(parseDecimal(token))
            }

            is Token.ConstantName -> {
                index++
                return Node.ConstantRef(token.name, token.pos)
            }

            is Token.FunctionName -> {
                index++
                if (current !is Token.LeftParen) {
                    throw CalcException(CalcError.Syntax, current.pos)
                }
                index++
                val argument = parseExpression()
                expectRightParen()
                return Node.Call(token.name, argument, token.pos)
            }

            is Token.LeftParen -> {
                index++
                val inner = parseExpression()
                expectRightParen()
                return inner
            }

            is Token.End -> throw CalcException(CalcError.Syntax, token.pos)

            else -> throw CalcException(CalcError.Syntax, token.pos)
        }
    }

    private fun expectRightParen() {
        val token = current
        if (token !is Token.RightParen) {
            throw CalcException(CalcError.UnbalancedParens, token.pos)
        }
        index++
    }

    private fun parseDecimal(token: Token.NumberLiteral): BigDecimal {
        // The tokenizer allows a Unicode minus inside an exponent; BigDecimal does not.
        val normalized = token.text.replace('−', '-')
        val value = try {
            BigDecimal(normalized)
        } catch (_: NumberFormatException) {
            throw CalcException(CalcError.Syntax, token.pos)
        }
        // `1E999999999` parses fine but would exhaust memory the moment it is used.
        if (NumberFormatter.isOutOfRange(value)) {
            throw CalcException(CalcError.Overflow, token.pos)
        }
        return value
    }
}
