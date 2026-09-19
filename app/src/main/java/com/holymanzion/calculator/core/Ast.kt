package com.holymanzion.calculator.core

import java.math.BigDecimal

/** Parsed expression tree. */
internal sealed interface Node {
    data class Literal(val value: BigDecimal) : Node

    data class ConstantRef(val name: String, val pos: Int) : Node

    data class Negate(val operand: Node) : Node

    data class Binary(val op: OperatorKind, val left: Node, val right: Node, val pos: Int) : Node

    data class Call(val name: String, val argument: Node, val pos: Int) : Node

    /**
     * A trailing `%`.
     *
     * Its meaning depends on where it sits: as the right operand of `+` or `-` it is
     * a percentage *of the left operand*, otherwise it is simply a division by 100.
     * [Evaluator] resolves that from context rather than the parser, because the
     * parser cannot know the operand value.
     */
    data class PercentOf(val operand: Node) : Node

    data class Factorial(val operand: Node, val pos: Int) : Node
}
