package com.holymanzion.calculator.tools

import java.math.BigInteger

/** The number bases the converter offers. */
enum class NumberBase(val label: String, val radix: Int, val prefix: String) {
    Binary("BIN", 2, "0b"),
    Octal("OCT", 8, "0o"),
    Decimal("DEC", 10, ""),
    Hexadecimal("HEX", 16, "0x"),
    ;

    /** The characters that are legal in this base, for filtering keyboard input. */
    val digits: String
        get() = DIGITS.take(radix)

    fun accepts(c: Char): Boolean = digits.contains(c.uppercaseChar())

    private companion object {
        const val DIGITS = "0123456789ABCDEF"
    }
}

/**
 * Integer conversion between number bases.
 *
 * Uses [BigInteger] rather than Long so a 64-bit hex value does not silently wrap to a
 * negative number — the single most common bug in base converters, and the reason the
 * answer here is arbitrary precision.
 */
object BaseConverter {

    /** @return null when [text] is not a valid number in [from]. */
    fun parse(text: String, from: NumberBase): BigInteger? {
        val cleaned = text.trim().removePrefix("-").uppercase()
        if (cleaned.isEmpty()) return null
        if (cleaned.any { !from.accepts(it) }) return null

        return try {
            val magnitude = BigInteger(cleaned, from.radix)
            if (text.trim().startsWith("-")) magnitude.negate() else magnitude
        } catch (_: NumberFormatException) {
            null
        }
    }

    /** Renders [value] in [to]. Letters are upper case, which is the usual convention. */
    fun format(value: BigInteger, to: NumberBase): String =
        value.toString(to.radix).uppercase()

    /** Converts directly between two bases, or null when the input does not parse. */
    fun convert(text: String, from: NumberBase, to: NumberBase): String? =
        parse(text, from)?.let { format(it, to) }

    /**
     * Groups digits for legibility: fours for binary, threes elsewhere.
     *
     * Binary is grouped in nibbles because that is how it maps onto hex — each group
     * of four is exactly one hex digit.
     */
    fun group(text: String, base: NumberBase): String {
        val negative = text.startsWith("-")
        val body = text.removePrefix("-")
        val size = if (base == NumberBase.Binary) 4 else 3
        if (body.length <= size) return text

        val grouped = body.reversed().chunked(size).joinToString(" ").reversed()
        return if (negative) "-$grouped" else grouped
    }

    /**
     * Bit width needed to hold [value], ignoring sign.
     *
     * Shown next to the result because "does this fit in 32 bits?" is usually the
     * actual question behind a base conversion.
     */
    fun bitLength(value: BigInteger): Int = value.abs().bitLength().coerceAtLeast(1)
}
