package com.holymanzion.calculator.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Every top-level screen, in the order the navigation drawer lists them.
 *
 * The app's navigation is a flat set of tools, so an enum is a better fit than a
 * navigation library: no graph, no arguments, and the drawer can be built by iterating
 * [entries].
 */
enum class Destination(
    val title: String,
    val icon: ImageVector,
    val description: String,
) {
    Calculator(
        title = "Calculator",
        icon = Icons.Filled.Calculate,
        description = "Basic and scientific",
    ),
    UnitConverter(
        title = "Unit Converter",
        icon = Icons.Filled.SwapHoriz,
        description = "Length, mass, temperature and more",
    ),
    Discount(
        title = "Discount",
        icon = Icons.Filled.LocalOffer,
        description = "Sale price and what you save",
    ),
    Price(
        title = "Price & Bill",
        icon = Icons.Filled.Payments,
        description = "Tax, tip and splitting",
    ),
    Loan(
        title = "Loan / EMI",
        icon = Icons.Filled.Savings,
        description = "Monthly repayment and interest",
    ),
    Percentage(
        title = "Percentage",
        icon = Icons.Filled.Percent,
        description = "Percentages and change",
    ),
    DateTools(
        title = "Date Calculator",
        icon = Icons.Filled.CalendarMonth,
        description = "Difference and date shifting",
    ),
    Bmi(
        title = "BMI",
        icon = Icons.Filled.MonitorHeart,
        description = "Body mass index",
    ),
    Settings(
        title = "Settings",
        icon = Icons.Filled.Settings,
        description = "Appearance, legal and about",
    ),
    ;

    companion object {
        /** Tools shown above the divider; [Settings] is pinned below it. */
        val tools: List<Destination> = entries.filter { it != Settings }
    }
}
