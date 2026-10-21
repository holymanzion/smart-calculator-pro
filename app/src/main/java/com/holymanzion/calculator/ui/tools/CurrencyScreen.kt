package com.holymanzion.calculator.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holymanzion.calculator.tools.currency.CurrencyConverter
import com.holymanzion.calculator.tools.currency.currencyDisplayName
import com.holymanzion.calculator.ui.CurrencyViewModel
import com.holymanzion.calculator.ui.components.DropdownField
import com.holymanzion.calculator.ui.components.LocalToolFormat
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold
import java.text.DateFormat
import java.util.Date

@Composable
fun CurrencyScreen(
    onOpenMenu: () -> Unit,
    viewModel: CurrencyViewModel = viewModel(factory = CurrencyViewModel.Factory),
) {
    val format = LocalToolFormat.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var amount by rememberSaveable { mutableStateOf("1") }
    var from by rememberSaveable { mutableStateOf("USD") }
    var to by rememberSaveable { mutableStateOf("GHS") }
    var useManualRate by rememberSaveable { mutableStateOf(false) }
    var manualRate by rememberSaveable { mutableStateOf("") }

    val codes = state.rates.codes
    val amountValue = amount.toDoubleOrNull()

    val effectiveRate = if (useManualRate) {
        manualRate.toDoubleOrNull()
    } else {
        CurrencyConverter.rateBetween(from, to, state.rates)
    }

    val converted = amountValue?.let { value ->
        effectiveRate?.let { CurrencyConverter.convertWithRate(value, it) }
    }

    ToolScaffold(title = "Currency", onOpenMenu = onOpenMenu) {
        NumberField(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount",
            suffix = from,
            imeAction = ImeAction.Done,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DropdownField(
                label = "From",
                options = codes,
                selected = from,
                optionLabel = { "$it — ${currencyDisplayName(it)}" },
                onSelect = { from = it },
                modifier = Modifier.weight(1f),
            )
            FilledTonalIconButton(
                onClick = {
                    val swap = from
                    from = to
                    to = swap
                },
            ) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Swap currencies")
            }
        }

        DropdownField(
            label = "To",
            options = codes,
            selected = to,
            optionLabel = { "$it — ${currencyDisplayName(it)}" },
            onSelect = { to = it },
        )

        ResultCard {
            when {
                state.rates.isEmpty && !useManualRate -> Text(
                    text = if (state.refreshing) {
                        "Fetching today's rates…"
                    } else {
                        "No rates yet. Tap Refresh, or switch on your own rate below to " +
                            "work offline."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                converted == null -> Text(
                    text = "Enter an amount to convert",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> {
                    ResultRow(
                        label = "${format.number(amountValue, 2)} $from",
                        value = "${format.number(converted, 2)} $to",
                        emphasised = true,
                    )
                    ResultDivider()
                    ResultRow(
                        label = "1 $from equals",
                        value = "${format.number(effectiveRate, 6)} $to",
                    )
                    if (effectiveRate != null && effectiveRate != 0.0) {
                        ResultRow(
                            label = "1 $to equals",
                            value = "${format.number(1.0 / effectiveRate, 6)} $from",
                        )
                    }
                }
            }
        }

        // ---- rate source --------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Use my own rate",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = useManualRate, onCheckedChange = { useManualRate = it })
        }

        if (useManualRate) {
            NumberField(
                value = manualRate,
                onValueChange = { manualRate = it },
                label = "1 $from in $to",
                imeAction = ImeAction.Done,
            )
            ToolNote(
                "Your own rate is used instead of the published one, and needs no " +
                    "internet connection. Useful when the rate you actually get differs " +
                    "from the mid-market figure.",
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rateAgeLabel(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.rates.isStale(System.currentTimeMillis())) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f),
                )
                if (state.refreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = { viewModel.refresh(force = true) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Text("  Refresh")
                    }
                }
            }

            ToolNote(
                "Mid-market rates, published once a day. Banks and bureaux add a margin, " +
                    "so what you are quoted will usually be a little worse.",
            )
        }
    }
}

/** One line describing how current the figures are, and why if they are not. */
private fun rateAgeLabel(state: com.holymanzion.calculator.ui.CurrencyUiState): String {
    val failedOffline = state.lastRefreshFailedOffline
    if (state.rates.isEmpty) {
        return when (failedOffline) {
            true -> "No connection"
            false -> "Could not reach the rate service"
            null -> "No rates yet"
        }
    }

    val updated = DateFormat.getDateInstance(DateFormat.MEDIUM)
        .format(Date(state.rates.providerUpdatedAtMillis))

    return when (failedOffline) {
        true -> "Offline — showing rates from $updated"
        false -> "Refresh failed — showing rates from $updated"
        null -> "Rates from $updated"
    }
}
