package com.holymanzion.calculator.tools.currency

import java.util.Currency
import java.util.Locale
import kotlin.math.abs

/**
 * A snapshot of exchange rates, all expressed against [base].
 *
 * Carries its own timestamps so the UI can say how old the figures are. Rates are a
 * perishable good and showing them without a date is how people end up quoting a
 * month-old number at a counter.
 */
data class CurrencyRates(
    val base: String,
    val rates: Map<String, Double>,
    /** When this device last fetched the data. */
    val fetchedAtMillis: Long,
    /** When the provider itself last recalculated, which is the figure that matters. */
    val providerUpdatedAtMillis: Long,
) {
    val isEmpty: Boolean get() = rates.isEmpty()

    /** Supported codes, sorted so the picker is predictable. */
    val codes: List<String> get() = rates.keys.sorted()

    fun ageMillis(nowMillis: Long): Long = abs(nowMillis - providerUpdatedAtMillis)

    /** True once the data is old enough to be worth warning about. */
    fun isStale(nowMillis: Long): Boolean = ageMillis(nowMillis) > STALE_AFTER_MILLIS

    /** True when a background refresh is worth attempting. */
    fun shouldRefresh(nowMillis: Long): Boolean =
        isEmpty || abs(nowMillis - fetchedAtMillis) > REFRESH_AFTER_MILLIS

    companion object {
        val EMPTY = CurrencyRates("USD", emptyMap(), 0L, 0L)

        /**
         * The provider recalculates once a day, so refreshing more often than this
         * spends the user's data for no new information.
         */
        const val REFRESH_AFTER_MILLIS: Long = 12 * 60 * 60 * 1000L

        /** Past this, the figures are old enough that the UI says so. */
        const val STALE_AFTER_MILLIS: Long = 3 * 24 * 60 * 60 * 1000L
    }
}

/**
 * Human-readable name for a currency code, from the platform's own tables.
 *
 * Avoids shipping a 166-entry name list that would need translating; the JDK already
 * has one, localised. Codes the platform does not recognise fall back to the code.
 */
fun currencyDisplayName(code: String): String = try {
    Currency.getInstance(code).getDisplayName(Locale.getDefault())
} catch (_: IllegalArgumentException) {
    code
}

/** Symbol for a currency code, or the code itself when there is no distinct symbol. */
fun currencySymbol(code: String): String = try {
    Currency.getInstance(code).getSymbol(Locale.getDefault())
} catch (_: IllegalArgumentException) {
    code
}
