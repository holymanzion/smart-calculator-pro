package com.holymanzion.calculator.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.holymanzion.calculator.tools.UnitCatalog
import com.holymanzion.calculator.tools.UnitCategory
import com.holymanzion.calculator.tools.convertUnits
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.DropdownField
import com.holymanzion.calculator.ui.components.LocalToolFormat
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold

@Composable
fun UnitConverterScreen(onOpenMenu: () -> Unit) {
    val format = LocalToolFormat.current
    var categoryOrdinal by rememberSaveable { mutableIntStateOf(0) }
    val category = UnitCategory.entries[categoryOrdinal]
    val units = UnitCatalog.unitsFor(category)

    var fromIndex by rememberSaveable { mutableIntStateOf(UnitCatalog.defaultFromIndex(category)) }
    var toIndex by rememberSaveable { mutableIntStateOf(UnitCatalog.defaultToIndex(category)) }
    var input by rememberSaveable { mutableStateOf("1") }

    // Indices are category-relative, so they have to be re-seeded whenever the
    // category changes or they would point at the wrong unit (or out of bounds).
    fun selectCategory(ordinal: Int) {
        categoryOrdinal = ordinal
        val next = UnitCategory.entries[ordinal]
        fromIndex = UnitCatalog.defaultFromIndex(next)
        toIndex = UnitCatalog.defaultToIndex(next)
    }

    val from = units.getOrElse(fromIndex) { units.first() }
    val to = units.getOrElse(toIndex) { units.last() }
    val amount = input.toDoubleOrNull()
    val converted = amount?.let { convertUnits(it, from, to) }

    ToolScaffold(title = "Unit Converter", onOpenMenu = onOpenMenu) {
        ChipSelector(
            options = UnitCategory.entries.map { it.label },
            selectedIndex = categoryOrdinal,
            onSelect = ::selectCategory,
        )

        NumberField(
            value = input,
            onValueChange = { input = it },
            label = "Value",
            suffix = from.symbol,
            allowNegative = category == UnitCategory.Temperature,
            imeAction = ImeAction.Done,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DropdownField(
                label = "From",
                options = units,
                selected = from,
                optionLabel = { "${it.name} (${it.symbol})" },
                onSelect = { fromIndex = units.indexOf(it) },
                modifier = Modifier.weight(1f),
            )
            FilledTonalIconButton(
                onClick = {
                    val swap = fromIndex
                    fromIndex = toIndex
                    toIndex = swap
                },
            ) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Swap units")
            }
        }

        DropdownField(
            label = "To",
            options = units,
            selected = to,
            optionLabel = { "${it.name} (${it.symbol})" },
            onSelect = { toIndex = units.indexOf(it) },
        )

        ResultCard {
            if (converted == null) {
                Text(
                    text = "Enter a value to convert",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ResultRow(
                    label = "${format.number(amount)} ${from.symbol}",
                    value = "${format.number(converted)} ${to.symbol}",
                    emphasised = true,
                )
                ResultRow(
                    label = "1 ${from.symbol} equals",
                    value = "${format.number(convertUnits(1.0, from, to))} ${to.symbol}",
                )
            }
        }

        if (category == UnitCategory.Data) {
            ToolNote(
                "Decimal units (kB, MB, GB) are powers of 1000; binary units " +
                    "(KiB, MiB, GiB) are powers of 1024. Storage is usually sold in the former " +
                    "and reported by operating systems in the latter.",
            )
        }
    }
}
