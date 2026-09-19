package com.holymanzion.calculator.ui.tools

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import com.holymanzion.calculator.tools.PercentageCalculator
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold
import com.holymanzion.calculator.ui.components.formatNumber

@Composable
fun PercentageScreen(onOpenMenu: () -> Unit) {
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    var first by rememberSaveable { mutableStateOf("") }
    var second by rememberSaveable { mutableStateOf("") }

    val mode = PercentageCalculator.Mode.entries[modeIndex]
    val x = first.toDoubleOrNull()
    val y = second.toDoubleOrNull()

    val answer: Double? = when {
        x == null || y == null -> null
        mode == PercentageCalculator.Mode.PercentOf -> PercentageCalculator.percentOf(x, y)
        mode == PercentageCalculator.Mode.WhatPercent -> PercentageCalculator.whatPercent(x, y)
        else -> PercentageCalculator.change(x, y)
    }

    val firstLabel = when (mode) {
        PercentageCalculator.Mode.PercentOf -> "Percentage (X)"
        PercentageCalculator.Mode.WhatPercent -> "Part (X)"
        PercentageCalculator.Mode.Change -> "Starting value"
    }
    val secondLabel = when (mode) {
        PercentageCalculator.Mode.PercentOf -> "Of value (Y)"
        PercentageCalculator.Mode.WhatPercent -> "Whole (Y)"
        PercentageCalculator.Mode.Change -> "Ending value"
    }

    ToolScaffold(title = "Percentage", onOpenMenu = onOpenMenu) {
        ChipSelector(
            options = listOf("X% of Y", "X is what % of Y", "% change"),
            selectedIndex = modeIndex,
            onSelect = { modeIndex = it },
        )

        Text(
            text = mode.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        NumberField(
            value = first,
            onValueChange = { first = it },
            label = firstLabel,
            suffix = if (mode == PercentageCalculator.Mode.PercentOf) "%" else null,
            allowNegative = true,
        )

        NumberField(
            value = second,
            onValueChange = { second = it },
            label = secondLabel,
            allowNegative = true,
            imeAction = ImeAction.Done,
        )

        ResultCard {
            when {
                x == null || y == null -> Text(
                    text = "Enter both values",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                answer == null -> Text(
                    text = if (mode == PercentageCalculator.Mode.Change) {
                        "Percentage change from zero is undefined — any increase would be infinite."
                    } else {
                        "A percentage of zero is undefined."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )

                else -> {
                    val suffix = if (mode == PercentageCalculator.Mode.PercentOf) "" else "%"
                    ResultRow(
                        label = "Answer",
                        value = "${formatNumber(answer, maxDecimals = 4)}$suffix",
                        emphasised = true,
                    )

                    if (mode == PercentageCalculator.Mode.Change) {
                        ResultDivider()
                        ResultRow(
                            label = if (answer >= 0) "Increase" else "Decrease",
                            value = formatNumber(kotlin.math.abs(y - x), maxDecimals = 4),
                        )
                    }
                }
            }
        }

        if (mode == PercentageCalculator.Mode.Change) {
            ToolNote(
                "A rise then an equal-looking fall do not cancel out: 100 up 50% is 150, " +
                    "and 150 down 50% is 75.",
            )
        }
    }
}
