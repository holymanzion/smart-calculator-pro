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
import com.holymanzion.calculator.tools.BmiCalculator
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.LocalToolFormat
import com.holymanzion.calculator.ui.components.NumberField
import com.holymanzion.calculator.ui.components.ResultCard
import com.holymanzion.calculator.ui.components.ResultDivider
import com.holymanzion.calculator.ui.components.ResultRow
import com.holymanzion.calculator.ui.components.ToolNote
import com.holymanzion.calculator.ui.components.ToolScaffold

@Composable
fun BmiScreen(onOpenMenu: () -> Unit) {
    val format = LocalToolFormat.current
    /** 0 = metric (cm, kg), 1 = imperial (ft/in, lb). */
    var unitIndex by rememberSaveable { mutableIntStateOf(0) }

    var heightCm by rememberSaveable { mutableStateOf("") }
    var feet by rememberSaveable { mutableStateOf("") }
    var inches by rememberSaveable { mutableStateOf("") }
    var weightKg by rememberSaveable { mutableStateOf("") }
    var weightLb by rememberSaveable { mutableStateOf("") }

    val metric = unitIndex == 0

    val resolvedHeightCm = if (metric) {
        heightCm.toDoubleOrNull()
    } else {
        val f = feet.toDoubleOrNull()
        val i = inches.toDoubleOrNull() ?: 0.0
        if (f == null) null else BmiCalculator.feetInchesToCm(f, i)
    }

    val resolvedWeightKg = if (metric) {
        weightKg.toDoubleOrNull()
    } else {
        weightLb.toDoubleOrNull()?.let { BmiCalculator.poundsToKg(it) }
    }

    val result = if (resolvedHeightCm != null && resolvedWeightKg != null) {
        BmiCalculator.calculate(resolvedWeightKg, resolvedHeightCm)
    } else {
        null
    }

    ToolScaffold(title = "BMI", onOpenMenu = onOpenMenu) {
        ChipSelector(
            options = listOf("Metric", "Imperial"),
            selectedIndex = unitIndex,
            onSelect = { unitIndex = it },
        )

        if (metric) {
            NumberField(
                value = heightCm,
                onValueChange = { heightCm = it },
                label = "Height",
                suffix = "cm",
            )
            NumberField(
                value = weightKg,
                onValueChange = { weightKg = it },
                label = "Weight",
                suffix = "kg",
                imeAction = ImeAction.Done,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NumberField(
                    value = feet,
                    onValueChange = { feet = it },
                    label = "Height",
                    suffix = "ft",
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = inches,
                    onValueChange = { inches = it },
                    label = " ",
                    suffix = "in",
                    modifier = Modifier.weight(1f),
                )
            }
            NumberField(
                value = weightLb,
                onValueChange = { weightLb = it },
                label = "Weight",
                suffix = "lb",
                imeAction = ImeAction.Done,
            )
        }

        ResultCard {
            if (result == null) {
                Text(
                    text = "Enter your height and weight",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ResultRow(
                    label = result.category.label,
                    value = format.number(result.bmi, maxDecimals = 1),
                    emphasised = true,
                )
                ResultDivider()
                ResultRow(label = "Category range", value = result.category.range)
                ResultRow(
                    label = "Healthy weight for your height",
                    value = if (metric) {
                        "${format.number(result.healthyMinKg, 1)} – " +
                            "${format.number(result.healthyMaxKg, 1)} kg"
                    } else {
                        "${format.number(result.healthyMinKg / 0.45359237, 1)} – " +
                            "${format.number(result.healthyMaxKg / 0.45359237, 1)} lb"
                    },
                )
            }
        }

        ToolNote(
            "BMI is a rough population-level screening figure, not a diagnosis. It takes " +
                "no account of muscle mass, build or age, so athletes often read as " +
                "overweight. Talk to a clinician before acting on it.",
        )
    }
}
