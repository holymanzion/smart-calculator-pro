package com.holymanzion.calculator.core

/**
 * Adds thousands separators to the expression *as displayed*.
 *
 * The stored expression is always kept separator-free so editing stays simple;
 * grouping is applied only at render time.
 */
object ExpressionFormatter {

    fun pretty(expression: String, grouped: Boolean = true): String {
        if (expression.isEmpty() || !grouped) return expression

        val out = StringBuilder(expression.length + expression.length / 3)
        var i = 0

        while (i < expression.length) {
            val c = expression[i]
            if (!c.isDigit() && c != '.') {
                out.append(c)
                i++
                continue
            }

            val start = i
            while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) i++
            val run = expression.substring(start, i)

            // Digits directly after `E` are an exponent, and digits after a letter are
            // part of an identifier such as `log2`. Neither should be grouped.
            val previous = if (start > 0) expression[start - 1] else ' '
            out.append(if (previous.isLetter()) run else groupRun(run))
        }

        return out.toString()
    }

    private fun groupRun(run: String): String {
        val dot = run.indexOf('.')
        val integerPart = if (dot >= 0) run.substring(0, dot) else run
        val rest = if (dot >= 0) run.substring(dot) else ""
        if (integerPart.length <= 3) return run

        val grouped = StringBuilder()
        val head = integerPart.length % 3
        if (head > 0) grouped.append(integerPart, 0, head)
        var i = head
        while (i < integerPart.length) {
            if (grouped.isNotEmpty()) grouped.append(',')
            grouped.append(integerPart, i, i + 3)
            i += 3
        }
        return grouped.append(rest).toString()
    }
}
