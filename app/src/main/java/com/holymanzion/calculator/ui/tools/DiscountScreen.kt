package com.holymanzion.calculator.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.holymanzion.calculator.tools.DiscountCalculator
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold
import com.holymanzion.calculator.ui.components.formatMoney
import com.holymanzion.calculator.ui.components.formatNumber

private val QUICK_DISCOUNTS = listOf(5, 10, 15, 20, 25, 30, 40, 50, 70)

@Composable
fun DiscountScreen(onOpenMenu: () -> Unit) {
    var price by rememberSaveable { mutableStateOf("") }
    var discount by rememberSaveable { mutableStateOf("20") }
    var extraDiscount by rememberSaveable { mutableStateOf("") }
    var tax by rememberSaveable { mutableStateOf("") }

    val priceValue = price.toDoubleOrNull()
    val result = priceValue?.let {
        DiscountCalculator.calculate(
            originalPrice = it,
            discountPercent = discount.toDoubleOrNull() ?: 0.0,
            extraDiscountPercent = extraDiscount.toDoubleOrNull() ?: 0.0,
            taxPercent = tax.toDoubleOrNull() ?: 0.0,
        )
    }

    val selectedQuick = QUICK_DISCOUNTS.indexOfFirst { it.toString() == discount }

    ToolScaffold(title = "Discount", onOpenMenu = onOpenMenu) {
        NumberField(
            value = price,
            onValueChange = { price = it },
            label = "Original price",
        )

        NumberField(
            value = discount,
            onValueChange = { discount = it },
            label = "Discount",
            suffix = "%",
        )

        ChipSelector(
            options = QUICK_DISCOUNTS.map { "$it%" },
            selectedIndex = selectedQuick,
            onSelect = { discount = QUICK_DISCOUNTS[it].toString() },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberField(
                value = extraDiscount,
                onValueChange = { extraDiscount = it },
                label = "Extra % off",
                suffix = "%",
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = tax,
                onValueChange = { tax = it },
                label = "Tax",
                suffix = "%",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Done,
            )
        }

        ResultCard {
            if (result == null) {
                Text(
                    text = "Enter a price to see the discount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ResultRow(
                    label = "You pay",
                    value = formatMoney(result.finalPrice),
                    emphasised = true,
                )
                ResultDivider()
                ResultRow(label = "Original price", value = formatMoney(priceValue))
                ResultRow(label = "Price after discount", value = formatMoney(result.discountedPrice))
                if (result.taxAmount > 0) {
                    ResultRow(label = "Tax added", value = formatMoney(result.taxAmount))
                }
                ResultDivider()
                ResultRow(label = "You save", value = formatMoney(result.youSave))
                ResultRow(
                    label = "Effective discount",
                    value = "${formatNumber(result.effectiveDiscountPercent, maxDecimals = 2)}%",
                )
            }
        }

        if ((extraDiscount.toDoubleOrNull() ?: 0.0) > 0) {
            ToolNote(
                "Stacked discounts multiply rather than add: the second percentage " +
                    "comes off the already-reduced price, so the effective discount is less " +
                    "than the two figures added together.",
            )
        }
    }
}
