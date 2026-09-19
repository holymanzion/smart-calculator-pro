package com.holymanzion.calculator.core

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

/**
 * Renders a computed [BigDecimal] for the display.
 *
 * Results are rounded to [DISPLAY_PRECISION] significant digits before anything else.
 * The evaluator deliberately works wider than that, and this is where the extra guard
 * digits get discarded, so accumulated floating-point tails never reach the screen.
 */
object NumberFormatter {

    /** Significant digits shown. Comfortably inside double precision. */
    const val DISPLAY_PRECISION: Int = 12

    /** At or above 1e12 the plain form is too long for a phone display. */
    private const val SCIENTIFIC_UPPER_EXPONENT = 12

    /** Below 1e-9 the plain form is mostly leading zeros. */
    private const val SCIENTIFIC_LOWER_EXPONENT = -7

    private val DISPLAY_MC = MathContext(DISPLAY_PRECISION, RoundingMode.HALF_UP)

    /**
     * Formats [value] for the result line.
     *
     * @param grouped whether to insert thousands separators in the integer part.
     */
    fun format(value: BigDecimal, grouped: Boolean = true): String {
        val rounded = value.round(DISPLAY_MC).stripTrailingZeros()
        if (rounded.signum() == 0) return "0"

        // BigDecimal's "adjusted exponent": the power of ten of the leading digit.
        val exponent = rounded.precision() - rounded.scale() - 1

        return if (exponent >= SCIENTIFIC_UPPER_EXPONENT || exponent <= SCIENTIFIC_LOWER_EXPONENT) {
            scientific(rounded, exponent)
        } else {
            val plain = rounded.toPlainString()
            if (grouped) group(plain) else plain
        }
    }

    /** The unformatted form used when a result is fed back into a new expression. */
    fun toExpressionText(value: BigDecimal): String = format(value, grouped = false)

    private fun scientific(rounded: BigDecimal, exponent: Int): String {
        val mantissa = rounded.movePointLeft(exponent).stripTrailingZeros()
        return "${mantissa.toPlainString()}E$exponent"
    }

    /** Inserts a separator every three digits, left of the decimal point only. */
    private fun group(plain: String): String {
        val negative = plain.startsWith("-")
        val body = if (negative) plain.substring(1) else plain

        val dot = body.indexOf('.')
        val integerPart = if (dot >= 0) body.substring(0, dot) else body
        val fractionPart = if (dot >= 0) body.substring(dot) else ""

        if (integerPart.length <= 3) return plain

        val builder = StringBuilder()
        val firstGroup = integerPart.length % 3
        if (firstGroup > 0) builder.append(integerPart, 0, firstGroup)
        var i = firstGroup
        while (i < integerPart.length) {
            if (builder.isNotEmpty()) builder.append(GROUP_SEPARATOR)
            builder.append(integerPart, i, i + 3)
            i += 3
        }

        return buildString {
            if (negative) append('-')
            append(builder)
            append(fractionPart)
        }
    }

    /** Strips separators so display text can be parsed again. */
    fun ungroup(text: String): String = text.replace(GROUP_SEPARATOR.toString(), "")

    /** True when the magnitude is too extreme to work with safely. */
    internal fun isOutOfRange(value: BigDecimal): Boolean {
        if (value.signum() == 0) return false
        return abs(value.precision() - value.scale() - 1) > MAX_EXPONENT
    }

    private const val GROUP_SEPARATOR = ','
    internal const val MAX_EXPONENT = 9999
}
