package com.holymanzion.calculator.tools.currency

/**
 * Currency conversion.
 *
 * Every rate is quoted against a single base, so converting between two arbitrary
 * currencies is a ratio rather than a lookup: `amount * (rate[to] / rate[from])`.
 * That means the base cancels out and USD-based data converts GHS to EUR perfectly
 * well, without a second request.
 *
 * Pure Kotlin with no Android or network dependency, so the arithmetic is covered by
 * plain JVM tests even though the fetching is not.
 */
object CurrencyConverter {

    /**
     * How many units of [to] one unit of [from] buys.
     *
     * @return null when either currency is missing from [rates], or when the source
     *   rate is zero or non-finite, which would otherwise yield infinity.
     */
    fun rateBetween(from: String, to: String, rates: CurrencyRates): Double? {
        val fromRate = rates.rates[from] ?: return null
        val toRate = rates.rates[to] ?: return null
        if (fromRate == 0.0 || !fromRate.isFinite() || !toRate.isFinite()) return null
        return toRate / fromRate
    }

    /** Converts [amount] using the rates in [rates]. Null when the pair is unavailable. */
    fun convert(amount: Double, from: String, to: String, rates: CurrencyRates): Double? {
        val rate = rateBetween(from, to, rates) ?: return null
        val result = amount * rate
        return if (result.isFinite()) result else null
    }

    /**
     * Converts using a rate the user typed in themselves.
     *
     * Exists because the official mid-market rate is often not the rate you actually
     * get, and because this path keeps working with no network at all.
     */
    fun convertWithRate(amount: Double, rate: Double): Double? {
        val result = amount * rate
        return if (result.isFinite()) result else null
    }
}
