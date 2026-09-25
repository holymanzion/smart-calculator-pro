package com.holymanzion.calculator.tools

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Date arithmetic.
 *
 * Uses `java.time`, which is API 26+; core library desugaring in the build script is
 * what makes it work down at minSdk 24.
 */
object DateCalculator {

    data class Difference(
        val totalDays: Long,
        val weeks: Long,
        val remainderDays: Long,
        val years: Int,
        val months: Int,
        val days: Int,
    )

    /**
     * Distance between two dates.
     *
     * Reported twice on purpose: a flat day count, and a calendar-aware
     * years/months/days breakdown. They disagree — months have different lengths — and
     * both readings are legitimate depending on what is being asked.
     */
    fun between(start: LocalDate, end: LocalDate): Difference {
        val earlier = if (start.isBefore(end)) start else end
        val later = if (start.isBefore(end)) end else start

        val totalDays = ChronoUnit.DAYS.between(earlier, later)
        val period = Period.between(earlier, later)

        return Difference(
            totalDays = totalDays,
            weeks = totalDays / 7,
            remainderDays = totalDays % 7,
            years = period.years,
            months = period.months,
            days = period.days,
        )
    }

    /**
     * Shifts [date] by the given amounts; negatives move backwards.
     *
     * Order matters: years and months are applied before days so that clamping at a
     * short month end (31 Jan + 1 month = 28 Feb) happens before the day offset.
     */
    fun shift(
        date: LocalDate,
        years: Long = 0,
        months: Long = 0,
        weeks: Long = 0,
        days: Long = 0,
    ): LocalDate = date
        .plusYears(years)
        .plusMonths(months)
        .plusWeeks(weeks)
        .plusDays(days)

    /** Age in whole years, plus the months and days past the last birthday. */
    fun age(birthDate: LocalDate, on: LocalDate = LocalDate.now()): Difference =
        between(birthDate, on)

    /**
     * Days until the next anniversary of [date], or 0 when it is today.
     *
     * A 29 February date is handled by `withYear`, which clamps to 28 February in a
     * non-leap year rather than throwing.
     */
    fun daysUntilNextAnniversary(date: LocalDate, from: LocalDate = LocalDate.now()): Long {
        var next = date.withYear(from.year)
        if (next.isBefore(from)) next = next.plusYears(1)
        return abs(ChronoUnit.DAYS.between(from, next))
    }
}
