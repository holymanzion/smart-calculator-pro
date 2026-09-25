package com.holymanzion.calculator.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.holymanzion.calculator.tools.PriceCalculator
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.LocalToolFormat
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold

private val QUICK_TIPS = listOf(0, 5, 10, 12, 15, 18, 20, 25)

@Composable
fun PriceScreen(onOpenMenu: () -> Unit) {
    val format = LocalToolFormat.current
    var amount by rememberSaveable { mutableStateOf("") }
    var taxPercent by rememberSaveable { mutableStateOf("") }
    var tipPercent by rememberSaveable { mutableStateOf("10") }
    var people by rememberSaveable { mutableStateOf("1") }
    var tipOnPreTax by rememberSaveable { mutableStateOf(true) }

    val amountValue = amount.toDoubleOrNull()
    val headCount = people.toIntOrNull()?.coerceAtLeast(1) ?: 1

    val result = amountValue?.let {
        PriceCalculator.calculate(
            amount = it,
            taxPercent = taxPercent.toDoubleOrNull() ?: 0.0,
            tipPercent = tipPercent.toDoubleOrNull() ?: 0.0,
            people = headCount,
            tipOnPreTaxAmount = tipOnPreTax,
        )
    }

    ToolScaffold(title = "Price & Bill", onOpenMenu = onOpenMenu) {
        NumberField(
            value = amount,
            onValueChange = { amount = it },
            label = "Bill amount",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberField(
                value = taxPercent,
                onValueChange = { taxPercent = it },
                label = "Tax",
                suffix = "%",
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = tipPercent,
                onValueChange = { tipPercent = it },
                label = "Tip",
                suffix = "%",
                modifier = Modifier.weight(1f),
            )
        }

        ChipSelector(
            options = QUICK_TIPS.map { "$it%" },
            selectedIndex = QUICK_TIPS.indexOfFirst { it.toString() == tipPercent },
            onSelect = { tipPercent = QUICK_TIPS[it].toString() },
        )

        NumberField(
            value = people,
            onValueChange = { people = it },
            label = "Split between",
            suffix = if (headCount == 1) "person" else "people",
            allowDecimal = false,
            imeAction = ImeAction.Done,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tip on pre-tax amount",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = tipOnPreTax, onCheckedChange = { tipOnPreTax = it })
        }

        ResultCard {
            if (result == null) {
                Text(
                    text = "Enter a bill amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ResultRow(
                    label = if (headCount > 1) "Each person pays" else "Total",
                    value = format.money(result.perPerson),
                    emphasised = true,
                )
                ResultDivider()
                ResultRow(label = "Subtotal", value = format.money(result.subtotal))
                ResultRow(label = "Tax", value = format.money(result.taxAmount))
                ResultRow(label = "Tip", value = format.money(result.tipAmount))
                ResultDivider()
                ResultRow(label = "Total", value = format.money(result.total))
                if (headCount > 1) {
                    ResultRow(label = "Split between", value = "$headCount people")
                }
            }
        }

        ToolNote(
            if (tipOnPreTax) {
                "Tip is calculated on the amount before tax, which is the usual convention."
            } else {
                "Tip is calculated on the amount after tax, so you are also tipping on the tax."
            },
        )
    }
}
