package com.holymanzion.calculator.tools

/**
 * The three percentage questions people actually ask, kept apart so the UI can label
 * each one in plain language instead of making the user work out which is which.
 */
object PercentageCalculator {

    enum class Mode(val label: String) {
        PercentOf("What is X% of Y?"),
        WhatPercent("X is what % of Y?"),
        Change("% change from X to Y"),
        AdjustBy("Increase or decrease by %"),
    }

    /**
     * Applies a percentage change to a value; a negative [percent] decreases it.
     *
     * Deliberately separate from [percentOf]: people asking "what's 20% off 80?" want
     * 64, not 16, and conflating the two is the most common percentage mistake.
     */
    fun adjustBy(value: Double, percent: Double): Double = value * (1 + percent / 100)

    /** X% of Y. */
    fun percentOf(percent: Double, value: Double): Double = percent / 100 * value

    /** X as a percentage of Y. Undefined when the whole is zero. */
    fun whatPercent(part: Double, whole: Double): Double? =
        if (whole == 0.0) null else part / whole * 100

    /**
     * Percentage change from [from] to [to]; negative means a decrease.
     * Undefined when starting from zero, since any increase is infinite.
     */
    fun change(from: Double, to: Double): Double? =
        if (from == 0.0) null else (to - from) / from * 100
}
