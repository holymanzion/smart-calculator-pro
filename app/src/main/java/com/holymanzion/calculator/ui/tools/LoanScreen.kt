package com.holymanzion.calculator.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.holymanzion.calculator.tools.LoanCalculator
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.LocalToolFormat
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold

@Composable
fun LoanScreen(onOpenMenu: () -> Unit) {
    val format = LocalToolFormat.current
    var principal by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("") }
    var term by rememberSaveable { mutableStateOf("") }
    /** 0 = term entered in years, 1 = in months. */
    var termUnit by rememberSaveable { mutableIntStateOf(0) }

    val principalValue = principal.toDoubleOrNull()
    val termValue = term.toDoubleOrNull()
    val months = termValue?.let { if (termUnit == 0) (it * 12).toInt() else it.toInt() }

    val result = if (principalValue != null && principalValue > 0 && months != null && months > 0) {
        LoanCalculator.calculate(principalValue, rate.toDoubleOrNull() ?: 0.0, months)
    } else {
        null
    }

    ToolScaffold(title = "Loan / EMI", onOpenMenu = onOpenMenu) {
        NumberField(
            value = principal,
            onValueChange = { principal = it },
            label = "Loan amount",
        )

        NumberField(
            value = rate,
            onValueChange = { rate = it },
            label = "Annual interest rate",
            suffix = "% per year",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberField(
                value = term,
                onValueChange = { term = it },
                label = "Term",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Done,
            )
        }

        ChipSelector(
            options = listOf("Years", "Months"),
            selectedIndex = termUnit,
            onSelect = { termUnit = it },
        )

        ResultCard {
            if (result == null) {
                Text(
                    text = "Enter an amount and a term to see the repayment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ResultRow(
                    label = "Monthly payment",
                    value = format.money(result.monthlyPayment),
                    emphasised = true,
                )
                ResultDivider()
                ResultRow(label = "Principal", value = format.money(principalValue))
                ResultRow(label = "Total interest", value = format.money(result.totalInterest))
                ResultRow(label = "Total payable", value = format.money(result.totalPayable))
                ResultDivider()
                ResultRow(label = "Number of payments", value = "$months")
                if (result.totalPayable > 0 && principalValue != null && principalValue > 0) {
                    ResultRow(
                        label = "Interest as % of loan",
                        value = "${format.number(result.totalInterest / principalValue * 100, 1)}%",
                    )
                }
            }
        }

        ToolNote(
            "Assumes a fixed rate and equal monthly instalments. Real quotes often add " +
                "arrangement fees and insurance, which are not included here.",
        )
    }
}
