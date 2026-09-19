package com.holymanzion.calculator.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode

/** Standard frame for a tool screen: app bar with the drawer button, scrolling body. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScaffold(
    title: String,
    onOpenMenu: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
            // Breathing room so the last card clears the navigation bar.
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Numeric text field.
 *
 * Input is filtered rather than validated after the fact, so the field can never hold
 * something unparseable — the screens can read it with `toDoubleOrNull` and show a
 * neutral empty state instead of an error.
 */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    allowDecimal: Boolean = true,
    allowNegative: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw -> onValueChange(sanitiseNumber(raw, allowDecimal, allowNegative)) },
        label = { Text(label) },
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number,
            imeAction = imeAction,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Keeps digits, at most one decimal point, and an optional leading minus. */
private fun sanitiseNumber(raw: String, allowDecimal: Boolean, allowNegative: Boolean): String {
    val negative = allowNegative && raw.startsWith("-")
    val builder = StringBuilder()
    var seenDot = false

    for (c in raw) {
        when {
            c.isDigit() -> builder.append(c)
            allowDecimal && (c == '.' || c == ',') && !seenDot -> {
                seenDot = true
                builder.append('.')
            }
        }
    }

    return if (negative) "-$builder" else builder.toString()
}

/**
 * Selection field backed by a plain [DropdownMenu].
 *
 * Deliberately not ExposedDropdownMenuBox: that API's anchor modifier has churned
 * across Material 3 releases, and nothing here needs its text-field behaviour.
 */
@Composable
fun <T> DropdownField(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = optionLabel(selected),
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Horizontally scrolling chip row used for the small mode switches. */
@Composable
fun ChipSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, option ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(option) },
            )
        }
    }
}

/** Container for a tool's computed output. */
@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

/** One label/value line inside a [ResultCard]. */
@Composable
fun ResultRow(
    label: String,
    value: String,
    emphasised: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (emphasised) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (emphasised) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = if (emphasised) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.bodyLarge
            },
            fontWeight = if (emphasised) FontWeight.SemiBold else FontWeight.Normal,
            color = if (emphasised) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
fun ResultDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** Explanatory line under a result, for caveats the number alone does not carry. */
@Composable
fun ToolNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

// ---- number rendering ----------------------------------------------------

/**
 * Fixed two decimal places with thousands separators, for money.
 *
 * Accepts null so screens can pass a field that has not been filled in yet without
 * each call site repeating the same fallback.
 */
fun formatMoney(value: Double?): String =
    if (value == null || !value.isFinite()) {
        "—"
    } else {
        groupInteger(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString())
    }

/**
 * General number rendering: up to [maxDecimals] places, trailing zeros removed, with
 * separators. Keeps converted units readable without inventing false precision.
 */
fun formatNumber(value: Double, maxDecimals: Int = 6): String {
    if (!value.isFinite()) return "—"

    val scaled = BigDecimal.valueOf(value)
        .setScale(maxDecimals, RoundingMode.HALF_UP)
        .stripTrailingZeros()

    // Very large or very small magnitudes are clearer in scientific notation.
    val exponent = scaled.precision() - scaled.scale() - 1
    if (scaled.signum() != 0 && (exponent >= 12 || exponent <= -7)) {
        return scaled.round(java.math.MathContext(8)).toString()
    }

    return groupInteger(scaled.toPlainString())
}

/** Inserts thousands separators into the integer part of a plain decimal string. */
private fun groupInteger(plain: String): String {
    val negative = plain.startsWith("-")
    val body = if (negative) plain.substring(1) else plain
    val dot = body.indexOf('.')
    val integerPart = if (dot >= 0) body.substring(0, dot) else body
    val fraction = if (dot >= 0) body.substring(dot) else ""

    if (integerPart.length <= 3) return plain

    val grouped = StringBuilder()
    val lead = integerPart.length % 3
    if (lead > 0) grouped.append(integerPart, 0, lead)
    var i = lead
    while (i < integerPart.length) {
        if (grouped.isNotEmpty()) grouped.append(',')
        grouped.append(integerPart, i, i + 3)
        i += 3
    }

    return buildString {
        if (negative) append('-')
        append(grouped)
        append(fraction)
    }
}
