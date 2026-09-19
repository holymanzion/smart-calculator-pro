package com.holymanzion.calculator.core

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

/**
 * Walks the parsed tree and produces a value.
 *
 * Arithmetic runs on [BigDecimal] at 34 significant digits, so the results people
 * actually notice stay clean (`0.1 + 0.2` is `0.3`, not `0.30000000000000004`).
 * Transcendental functions have no exact decimal form, so those round-trip through
 * [Double]; the extra guard digits mean the noise falls off well below what
 * [NumberFormatter] shows.
 */
internal class Evaluator(
    private val angleMode: AngleMode,
    private val ans: BigDecimal,
) {

    fun evaluate(node: Node): BigDecimal = eval(node, percentBase = null)

    /**
     * [percentBase] is the value a trailing `%` should be taken *of*. It is non-null
     * only while evaluating the right operand of `+` or `-`, which is what makes
     * `200 + 10%` equal 220 while `200 * 10%` equals 20.
     */
    private fun eval(node: Node, percentBase: BigDecimal?): BigDecimal = when (node) {
        is Node.Literal -> node.value

        is Node.ConstantRef -> when (node.name) {
            "pi" -> PI
            "e" -> E
            "ans" -> ans
            else -> throw CalcException(CalcError.Syntax, node.pos)
        }

        // The base propagates through negation so `200 - -10%` stays meaningful.
        is Node.Negate -> eval(node.operand, percentBase).negate()

        is Node.PercentOf -> {
            val value = eval(node.operand, null)
            if (percentBase != null) {
                percentBase.multiply(value, MC).divide(HUNDRED, MC)
            } else {
                value.divide(HUNDRED, MC)
            }
        }

        is Node.Factorial -> factorial(eval(node.operand, null), node.pos)

        is Node.Call -> applyFunction(node.name, eval(node.argument, null), node.pos)

        is Node.Binary -> {
            val left = eval(node.left, null)
            when (node.op) {
                OperatorKind.Plus -> left.add(eval(node.right, left), MC)
                OperatorKind.Minus -> left.subtract(eval(node.right, left), MC)
                OperatorKind.Times -> left.multiply(eval(node.right, null), MC)
                OperatorKind.Divide -> {
                    val right = eval(node.right, null)
                    if (right.signum() == 0) throw CalcException(CalcError.DivisionByZero, node.pos)
                    left.divide(right, MC)
                }

                OperatorKind.Power -> power(left, eval(node.right, null), node.pos)
            }
        }
    }

    // ---- operators -------------------------------------------------------

    private fun power(base: BigDecimal, exponent: BigDecimal, pos: Int): BigDecimal {
        // An integer exponent of modest size is worth doing exactly.
        val asInteger = exponent.toBigIntegerOrNull()
        if (asInteger != null && asInteger.abs() <= MAX_EXACT_POWER) {
            val n = asInteger.toInt()
            if (base.signum() == 0 && n < 0) throw CalcException(CalcError.DivisionByZero, pos)
            return if (n >= 0) base.pow(n, MC) else BigDecimal.ONE.divide(base.pow(-n, MC), MC)
        }

        val b = base.toDouble()
        val e = exponent.toDouble()
        if (b == 0.0 && e < 0) throw CalcException(CalcError.DivisionByZero, pos)
        // A negative base raised to a non-integer power has no real value.
        if (b < 0) throw CalcException(CalcError.Domain, pos)
        return Math.pow(b, e).toDecimal(pos)
    }

    private fun factorial(value: BigDecimal, pos: Int): BigDecimal {
        val n = value.toBigIntegerOrNull() ?: throw CalcException(CalcError.Domain, pos)
        if (n.signum() < 0) throw CalcException(CalcError.Domain, pos)
        if (n > MAX_FACTORIAL) throw CalcException(CalcError.Overflow, pos)

        var acc = BigInteger.ONE
        for (i in 2..n.toInt()) {
            acc = acc.multiply(BigInteger.valueOf(i.toLong()))
        }
        return BigDecimal(acc)
    }

    // ---- functions -------------------------------------------------------

    private fun applyFunction(name: String, arg: BigDecimal, pos: Int): BigDecimal = when (name) {
        "sin" -> trig(name, arg, pos)
        "cos" -> trig(name, arg, pos)
        "tan" -> trig(name, arg, pos)

        "asin" -> {
            val x = arg.toDouble()
            if (x < -1.0 || x > 1.0) throw CalcException(CalcError.Domain, pos)
            fromRadians(asin(x)).toDecimal(pos)
        }

        "acos" -> {
            val x = arg.toDouble()
            if (x < -1.0 || x > 1.0) throw CalcException(CalcError.Domain, pos)
            fromRadians(acos(x)).toDecimal(pos)
        }

        "atan" -> fromRadians(atan(arg.toDouble())).toDecimal(pos)

        "sinh" -> sinh(arg.toDouble()).toDecimal(pos)
        "cosh" -> cosh(arg.toDouble()).toDecimal(pos)
        "tanh" -> tanh(arg.toDouble()).toDecimal(pos)

        "ln" -> {
            if (arg.signum() <= 0) throw CalcException(CalcError.Domain, pos)
            ln(arg.toDouble()).toDecimal(pos)
        }

        "log" -> {
            if (arg.signum() <= 0) throw CalcException(CalcError.Domain, pos)
            log10(arg.toDouble()).toDecimal(pos)
        }

        "log2" -> {
            if (arg.signum() <= 0) throw CalcException(CalcError.Domain, pos)
            log2(arg.toDouble()).toDecimal(pos)
        }

        "sqrt" -> {
            if (arg.signum() < 0) throw CalcException(CalcError.Domain, pos)
            // BigDecimal.sqrt needs API 31; minSdk is 24, so this goes through Double.
            sqrt(arg.toDouble()).toDecimal(pos)
        }

        "cbrt" -> Math.cbrt(arg.toDouble()).toDecimal(pos)
        "exp" -> exp(arg.toDouble()).toDecimal(pos)

        "abs" -> arg.abs()
        "floor" -> arg.setScale(0, RoundingMode.FLOOR)
        "ceil" -> arg.setScale(0, RoundingMode.CEILING)
        "round" -> arg.setScale(0, RoundingMode.HALF_UP)

        else -> throw CalcException(CalcError.Syntax, pos)
    }

    /**
     * Trigonometry, with exact answers at the quadrant boundaries.
     *
     * In degree mode `sin(180)` through a double would surface as `1.22E-16`, which
     * looks broken on a calculator display. Whole multiples of 90° are therefore
     * answered from a table instead of computed.
     */
    private fun trig(name: String, arg: BigDecimal, pos: Int): BigDecimal {
        if (angleMode == AngleMode.Degrees && arg.remainder(NINETY).signum() == 0) {
            val quadrant = arg.divideToIntegralValue(NINETY).toBigInteger().mod(FOUR).toInt()
            val exact = when (name) {
                "sin" -> SIN_QUADRANTS[quadrant]
                "cos" -> COS_QUADRANTS[quadrant]
                else -> {
                    // tan is undefined at 90° and 270°.
                    if (quadrant % 2 == 1) throw CalcException(CalcError.Domain, pos)
                    0
                }
            }
            return BigDecimal(exact)
        }

        val radians = toRadians(arg.toDouble())
        val result = when (name) {
            "sin" -> sin(radians)
            "cos" -> cos(radians)
            else -> {
                // Guard the poles in radian mode too, where the argument is never exact.
                if (kotlin.math.abs(cos(radians)) < TAN_POLE_EPSILON) {
                    throw CalcException(CalcError.Domain, pos)
                }
                tan(radians)
            }
        }
        return result.toDecimal(pos)
    }

    private fun toRadians(value: Double): Double =
        if (angleMode == AngleMode.Degrees) Math.toRadians(value) else value

    private fun fromRadians(value: Double): Double =
        if (angleMode == AngleMode.Degrees) Math.toDegrees(value) else value

    // ---- conversions -----------------------------------------------------

    private fun Double.toDecimal(pos: Int): BigDecimal {
        if (isNaN()) throw CalcException(CalcError.Domain, pos)
        if (isInfinite()) throw CalcException(CalcError.Overflow, pos)
        // valueOf goes via Double.toString, i.e. the shortest decimal that round-trips.
        // The BigDecimal(double) constructor would instead expose the full binary tail
        // and turn 0.1 into 0.1000000000000000055511151231257827.
        return BigDecimal.valueOf(this)
    }

    private fun BigDecimal.toBigIntegerOrNull(): BigInteger? = try {
        toBigIntegerExact()
    } catch (_: ArithmeticException) {
        null
    }

    companion object {
        /** Working precision. Comfortably wider than what is ever displayed. */
        val MC: MathContext = MathContext(34, RoundingMode.HALF_EVEN)

        private val SIN_QUADRANTS = intArrayOf(0, 1, 0, -1)
        private val COS_QUADRANTS = intArrayOf(1, 0, -1, 0)

        private val PI = BigDecimal("3.141592653589793238462643383279503")
        private val E = BigDecimal("2.718281828459045235360287471352662")
        private val HUNDRED = BigDecimal(100)
        private val NINETY = BigDecimal(90)
        private val FOUR = BigInteger.valueOf(4)

        private val MAX_EXACT_POWER = BigInteger.valueOf(9_999)
        private val MAX_FACTORIAL = BigInteger.valueOf(1_000)
        private const val TAN_POLE_EPSILON = 1e-15
    }
}
