package com.holymanzion.calculator.ui

import androidx.annotation.StringRes

/**
 * A press on the keypad.
 *
 * The keypad emits these and [CalculatorScreen] maps them onto [CalculatorViewModel],
 * which keeps the layout free of editing logic.
 */
sealed interface CalcKey {
    data class Digit(val value: String) : CalcKey

    data class Operator(val symbol: Char) : CalcKey

    /** A named function; inserted as `name(`. */
    data class Function(val name: String) : CalcKey

    /** Text that begins a new operand, so it replaces a result after `=`. */
    data class Operand(val text: String) : CalcKey

    /** Text that continues the current expression, such as `^2`. */
    data class Append(val text: String) : CalcKey

    data object Dot : CalcKey
    data object Equals : CalcKey
    data object Clear : CalcKey
    data object Backspace : CalcKey
    data object Parenthesis : CalcKey
    data object Percent : CalcKey
    data object Factorial : CalcKey
    data object ToggleSign : CalcKey
    data object ToggleSecond : CalcKey
    data object ToggleAngle : CalcKey
}

/** Visual weight of a key, resolved to theme colours in [CalcButton]. */
enum class KeyStyle {
    Digit,
    Operator,
    Accent,
    Function,
    Destructive,
}

data class KeySpec(
    val label: String,
    val key: CalcKey,
    val style: KeyStyle,
    @param:StringRes val contentDescription: Int? = null,
    val selected: Boolean = false,
)
