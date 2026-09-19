package com.holymanzion.calculator.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.holymanzion.calculator.tools.DateCalculator
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val MILLIS_PER_DAY = 86_400_000L

@Composable
fun DateScreen(onOpenMenu: () -> Unit) {
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val today = remember { LocalDate.now() }

    var startEpochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var endEpochDay by rememberSaveable { mutableLongStateOf(today.plusDays(30).toEpochDay()) }
    var baseEpochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }

    var years by rememberSaveable { mutableStateOf("") }
    var months by rememberSaveable { mutableStateOf("") }
    var weeks by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableStateOf("") }
    /** 0 = add to the date, 1 = subtract from it. */
    var directionIndex by rememberSaveable { mutableIntStateOf(0) }

    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    ToolScaffold(title = "Date Calculator", onOpenMenu = onOpenMenu) {
        ChipSelector(
            options = listOf("Difference", "Add or subtract"),
            selectedIndex = modeIndex,
            onSelect = { modeIndex = it },
        )

        if (modeIndex == 0) {
            val start = LocalDate.ofEpochDay(startEpochDay)
            val end = LocalDate.ofEpochDay(endEpochDay)
            val difference = DateCalculator.between(start, end)

            DateField("From", start, formatter) { startEpochDay = it.toEpochDay() }
            DateField("To", end, formatter) { endEpochDay = it.toEpochDay() }

            ResultCard {
                ResultRow(
                    label = "Total days",
                    value = "${difference.totalDays}",
                    emphasised = true,
                )
                ResultDivider()
                ResultRow(
                    label = "In weeks",
                    value = if (difference.remainderDays == 0L) {
                        "${difference.weeks} weeks"
                    } else {
                        "${difference.weeks} weeks, ${difference.remainderDays} days"
                    },
                )
                ResultRow(
                    label = "In calendar terms",
                    value = buildString {
                        if (difference.years > 0) append("${difference.years}y ")
                        if (difference.months > 0) append("${difference.months}m ")
                        append("${difference.days}d")
                    }.trim(),
                )
            }

            ToolNote(
                "The two readings differ because calendar months vary in length. " +
                    "The day count is exact; the years/months breakdown follows the calendar.",
            )
        } else {
            val base = LocalDate.ofEpochDay(baseEpochDay)
            val sign = if (directionIndex == 0) 1L else -1L
            val result = DateCalculator.shift(
                date = base,
                years = sign * (years.toLongOrNull() ?: 0L),
                months = sign * (months.toLongOrNull() ?: 0L),
                weeks = sign * (weeks.toLongOrNull() ?: 0L),
                days = sign * (days.toLongOrNull() ?: 0L),
            )

            DateField("Starting date", base, formatter) { baseEpochDay = it.toEpochDay() }

            ChipSelector(
                options = listOf("Add", "Subtract"),
                selectedIndex = directionIndex,
                onSelect = { directionIndex = it },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberField(
                    value = years,
                    onValueChange = { years = it },
                    label = "Years",
                    allowDecimal = false,
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = months,
                    onValueChange = { months = it },
                    label = "Months",
                    allowDecimal = false,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberField(
                    value = weeks,
                    onValueChange = { weeks = it },
                    label = "Weeks",
                    allowDecimal = false,
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = days,
                    onValueChange = { days = it },
                    label = "Days",
                    allowDecimal = false,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Done,
                )
            }

            ResultCard {
                ResultRow(
                    label = "Result",
                    value = result.format(formatter),
                    emphasised = true,
                )
                ResultDivider()
                ResultRow(label = "Day of week", value = result.dayOfWeek.toDisplayName())
                ResultRow(
                    label = "Days from today",
                    value = "${DateCalculator.between(today, result).totalDays}",
                )
            }

            ToolNote(
                "Years and months are applied before days, so a short month end is " +
                    "clamped first: 31 January plus one month is 28 February.",
            )
        }
    }
}

/** Tappable field that opens the Material date picker. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    formatter: DateTimeFormatter,
    onDateChange: (LocalDate) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            Text(
                text = "  ${date.format(formatter)}",
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * MILLIS_PER_DAY,
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            // The picker reports UTC midnight; floorDiv keeps dates
                            // before 1970 from rounding the wrong way.
                            onDateChange(LocalDate.ofEpochDay(Math.floorDiv(millis, MILLIS_PER_DAY)))
                        }
                        showPicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

/** "MONDAY" reads poorly in a result row. */
private fun java.time.DayOfWeek.toDisplayName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }
