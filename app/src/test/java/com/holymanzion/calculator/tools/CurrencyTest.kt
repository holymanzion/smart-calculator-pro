package com.holymanzion.calculator.tools

import com.holymanzion.calculator.tools.currency.CurrencyConverter
import com.holymanzion.calculator.tools.currency.CurrencyRates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyTest {

    private val now = 1_700_000_000_000L

    private val rates = CurrencyRates(
        base = "USD",
        rates = mapOf(
            "USD" to 1.0,
            "GHS" to 11.654668,
            "EUR" to 0.89075,
            "GBP" to 0.748335,
        ),
        fetchedAtMillis = now,
        providerUpdatedAtMillis = now,
    )

    // ---- conversion ------------------------------------------------------

    @Test
    fun `converting from the base currency uses the rate directly`() {
        assertEquals(11.654668, CurrencyConverter.convert(1.0, "USD", "GHS", rates)!!, 1e-9)
        assertEquals(116.54668, CurrencyConverter.convert(10.0, "USD", "GHS", rates)!!, 1e-9)
    }

    @Test
    fun `converting to the base currency inverts the rate`() {
        val result = CurrencyConverter.convert(11.654668, "GHS", "USD", rates)!!
        assertEquals(1.0, result, 1e-9)
    }

    @Test
    fun `a pair that excludes the base still converts`() {
        // The base cancels out, so USD-quoted data handles GHS to EUR without a
        // second request. 11.654668 GHS is 1 USD is 0.89075 EUR.
        val result = CurrencyConverter.convert(11.654668, "GHS", "EUR", rates)!!
        assertEquals(0.89075, result, 1e-9)
    }

    @Test
    fun `converting a currency to itself is identity`() {
        assertEquals(42.0, CurrencyConverter.convert(42.0, "GHS", "GHS", rates)!!, 1e-9)
    }

    @Test
    fun `round tripping returns the original amount`() {
        val there = CurrencyConverter.convert(250.0, "GBP", "GHS", rates)!!
        val back = CurrencyConverter.convert(there, "GHS", "GBP", rates)!!
        assertEquals(250.0, back, 1e-9)
    }

    @Test
    fun `rate between two currencies is the ratio of their rates`() {
        val rate = CurrencyConverter.rateBetween("EUR", "GBP", rates)!!
        assertEquals(0.748335 / 0.89075, rate, 1e-12)
    }

    // ---- missing and degenerate data -------------------------------------

    @Test
    fun `an unknown currency yields null rather than a wrong number`() {
        assertNull(CurrencyConverter.convert(1.0, "USD", "XYZ", rates))
        assertNull(CurrencyConverter.convert(1.0, "XYZ", "USD", rates))
        assertNull(CurrencyConverter.rateBetween("XYZ", "ABC", rates))
    }

    @Test
    fun `a zero source rate does not produce infinity`() {
        val broken = rates.copy(rates = rates.rates + ("BAD" to 0.0))
        assertNull(CurrencyConverter.rateBetween("BAD", "USD", broken))
    }

    @Test
    fun `an empty snapshot converts nothing`() {
        assertNull(CurrencyConverter.convert(1.0, "USD", "GHS", CurrencyRates.EMPTY))
        assertTrue(CurrencyRates.EMPTY.isEmpty)
    }

    @Test
    fun `a manual rate bypasses the table entirely`() {
        assertEquals(1250.0, CurrencyConverter.convertWithRate(100.0, 12.5)!!, 1e-9)
    }

    // ---- freshness -------------------------------------------------------

    @Test
    fun `fresh rates are neither stale nor due for refresh`() {
        assertFalse(rates.isStale(now))
        assertFalse(rates.shouldRefresh(now))
    }

    @Test
    fun `rates become due for refresh after twelve hours`() {
        val justUnder = now + CurrencyRates.REFRESH_AFTER_MILLIS - 1
        val justOver = now + CurrencyRates.REFRESH_AFTER_MILLIS + 1
        assertFalse(rates.shouldRefresh(justUnder))
        assertTrue(rates.shouldRefresh(justOver))
    }

    @Test
    fun `rates are flagged stale after three days`() {
        val justUnder = now + CurrencyRates.STALE_AFTER_MILLIS - 1
        val justOver = now + CurrencyRates.STALE_AFTER_MILLIS + 1
        assertFalse(rates.isStale(justUnder))
        assertTrue(rates.isStale(justOver))
    }

    @Test
    fun `an empty snapshot always wants refreshing`() {
        assertTrue(CurrencyRates.EMPTY.shouldRefresh(now))
    }

    @Test
    fun `codes are sorted so the picker is predictable`() {
        assertEquals(listOf("EUR", "GBP", "GHS", "USD"), rates.codes)
    }
}
