package com.holymanzion.calculator.core

/** Unit that trigonometric arguments (and inverse-trig results) are expressed in. */
enum class AngleMode(val label: String) {
    Degrees("DEG"),
    Radians("RAD"),
    ;

    fun toggled(): AngleMode = if (this == Degrees) Radians else Degrees
}
