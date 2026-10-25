package com.holymanzion.calculator.ui.tools

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.holymanzion.calculator.tools.BaseConverter
import com.holymanzion.calculator.tools.NumberBase
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold

@Composable
fun BaseConverterScreen(onOpenMenu: () -> Unit) {
    var baseOrdinal by rememberSaveable { mutableIntStateOf(NumberBase.Decimal.ordinal) }
    var input by rememberSaveable { mutableStateOf("") }

    val base = NumberBase.entries[baseOrdinal]
    val parsed = BaseConverter.parse(input, base)

    // Switching base keeps the typed digits only when they remain legal there, which
    // is the behaviour that surprises least: 1010 stays, FF does not survive a move
    // to decimal.
    fun selectBase(ordinal: Int) {
        val next = NumberBase.entries[ordinal]
        val current = BaseConverter.parse(input, base)
        baseOrdinal = ordinal
        input = current?.let { BaseConverter.format(it, next) } ?: ""
    }

    ToolScaffold(title = "Number Base", onOpenMenu = onOpenMenu) {
        ChipSelector(
            options = NumberBase.entries.map { it.label },
            selectedIndex = baseOrdinal,
            onSelect = ::selectBase,
        )

        OutlinedTextField(
            value = input,
            onValueChange = { raw ->
                // Filter to the digits this base allows, so the field can never hold
                // something unparseable.
                input = raw.uppercase().filter { it == '-' || base.accepts(it) }
            },
            label = { Text("Value in ${base.label}") },
            prefix = if (base.prefix.isNotEmpty()) {
                { Text(base.prefix) }
            } else {
                null
            },
            singleLine = true,
            isError = input.isNotEmpty() && parsed == null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (base == NumberBase.Decimal) {
                    KeyboardType.Number
                } else {
                    KeyboardType.Ascii
                },
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        ResultCard {
            if (parsed == null) {
                Text(
                    text = if (input.isEmpty()) {
                        "Enter a value to convert"
                    } else {
                        "Not a valid ${base.label} number"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (input.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            } else {
                NumberBase.entries.forEachIndexed { index, target ->
                    val rendered = BaseConverter.format(parsed, target)
                    ResultRow(
                        label = target.label,
                        value = target.prefix + BaseConverter.group(rendered, target),
                        emphasised = target == base,
                    )
                    if (index == NumberBase.entries.lastIndex) ResultDivider()
                }
                ResultRow(label = "Bits needed", value = "${BaseConverter.bitLength(parsed)}")
            }
        }

        ToolNote(
            "Whole numbers only, at any size — values are handled with arbitrary " +
                "precision, so a full 64-bit hex value will not wrap round to a " +
                "negative number. Binary is grouped in fours because each group is " +
                "exactly one hex digit.",
        )
    }
}
