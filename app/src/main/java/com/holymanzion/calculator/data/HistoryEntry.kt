package com.holymanzion.calculator.data

/** One completed calculation, as shown in the history panel. */
data class HistoryEntry(
    /** The expression exactly as it was entered, already prettified for display. */
    val expression: String,
    /** The formatted result. */
    val result: String,
    /** Raw result text, suitable for feeding back into a new expression. */
    val rawResult: String,
    val timestamp: Long,
)
