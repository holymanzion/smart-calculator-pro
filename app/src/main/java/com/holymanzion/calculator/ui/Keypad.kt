package com.holymanzion.calculator.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.holymanzion.calculator.R
import com.holymanzion.calculator.ui.theme.KeyTextStyle
import com.holymanzion.calculator.ui.theme.SmallKeyTextStyle

/**
 * The key grid.
 *
 * Every row is weighted, so the pad simply fills whatever height it is given and the
 * scientific rows appearing or disappearing rebalances the rest automatically.
 */
@Composable
fun Keypad(
    state: CalculatorUiState,
    onKey: (CalcKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.scientific) {
            scientificRows(state).forEach { row ->
                KeyRow(row, onKey, compact = true, modifier = Modifier.weight(SCIENTIFIC_ROW_WEIGHT))
            }
        }
        BasicRows.forEach { row ->
            KeyRow(row, onKey, compact = false, modifier = Modifier.weight(BASIC_ROW_WEIGHT))
        }
    }
}

@Composable
private fun ColumnScope.KeyRow(
    specs: List<KeySpec>,
    onKey: (CalcKey) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        specs.forEach { spec ->
            CalcButton(
                spec = spec,
                onKey = onKey,
                compact = compact,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun RowScope.CalcButton(
    spec: KeySpec,
    onKey: (CalcKey) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val palette = spec.style.palette(spec.selected)
    val description = spec.contentDescription?.let { stringResource(it) }
    val hapticsEnabled = LocalHapticsEnabled.current

    Surface(
        onClick = {
            // Matches the feel of the system keyboard rather than a generic click.
            if (hapticsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            onKey(spec.key)
        },
        modifier = modifier.then(
            if (description != null) {
                Modifier.semantics { contentDescription = description }
            } else {
                Modifier
            },
        ),
        shape = RoundedCornerShape(if (compact) 16.dp else 22.dp),
        color = palette.container,
        contentColor = palette.content,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = spec.label,
                style = if (compact) SmallKeyTextStyle else KeyTextStyle,
                maxLines = 1,
            )
        }
    }
}

private data class KeyPalette(val container: Color, val content: Color)

@Composable
private fun KeyStyle.palette(selected: Boolean): KeyPalette {
    val scheme = MaterialTheme.colorScheme
    // A selected toggle reads as "on" by borrowing the primary colour.
    if (selected) return KeyPalette(scheme.primary, scheme.onPrimary)

    return when (this) {
        KeyStyle.Digit -> KeyPalette(scheme.surfaceContainerHigh, scheme.onSurface)
        KeyStyle.Operator -> KeyPalette(scheme.secondaryContainer, scheme.onSecondaryContainer)
        KeyStyle.Accent -> KeyPalette(scheme.primary, scheme.onPrimary)
        KeyStyle.Function -> KeyPalette(scheme.surfaceContainer, scheme.onSurfaceVariant)
        KeyStyle.Destructive -> KeyPalette(scheme.tertiaryContainer, scheme.onTertiaryContainer)
    }
}

// ---- layouts -------------------------------------------------------------

private const val SCIENTIFIC_ROW_WEIGHT = 1f

/** Main keys are a touch taller than the scientific ones, which are used less often. */
private const val BASIC_ROW_WEIGHT = 1.25f

private fun digit(value: String) = KeySpec(value, CalcKey.Digit(value), KeyStyle.Digit)

private val BasicRows: List<List<KeySpec>> = listOf(
    listOf(
        KeySpec("AC", CalcKey.Clear, KeyStyle.Destructive, R.string.cd_clear),
        KeySpec("( )", CalcKey.Parenthesis, KeyStyle.Operator, R.string.cd_parenthesis),
        KeySpec("%", CalcKey.Percent, KeyStyle.Operator, R.string.cd_percent),
        KeySpec("÷", CalcKey.Operator('÷'), KeyStyle.Operator, R.string.cd_divide),
    ),
    listOf(
        digit("7"), digit("8"), digit("9"),
        KeySpec("×", CalcKey.Operator('×'), KeyStyle.Operator, R.string.cd_multiply),
    ),
    listOf(
        digit("4"), digit("5"), digit("6"),
        KeySpec("−", CalcKey.Operator('−'), KeyStyle.Operator, R.string.cd_subtract),
    ),
    listOf(
        digit("1"), digit("2"), digit("3"),
        KeySpec("+", CalcKey.Operator('+'), KeyStyle.Operator, R.string.cd_add),
    ),
    listOf(
        KeySpec("+/−", CalcKey.ToggleSign, KeyStyle.Digit, R.string.cd_toggle_sign),
        digit("0"),
        KeySpec(".", CalcKey.Dot, KeyStyle.Digit, R.string.cd_decimal),
        KeySpec("=", CalcKey.Equals, KeyStyle.Accent, R.string.cd_equals),
    ),
)

/**
 * The scientific rows, whose middle two rows swap to their inverse counterparts when
 * `2nd` is held on.
 */
private fun scientificRows(state: CalculatorUiState): List<List<KeySpec>> {
    val inverse = state.secondFunction

    return listOf(
        listOf(
            KeySpec(
                label = "2nd",
                key = CalcKey.ToggleSecond,
                style = KeyStyle.Function,
                contentDescription = R.string.cd_second_function,
                selected = inverse,
            ),
            KeySpec(
                label = state.angleMode.label,
                key = CalcKey.ToggleAngle,
                style = KeyStyle.Function,
                contentDescription = R.string.cd_angle_mode,
            ),
            KeySpec(
                if (inverse) "sin⁻¹" else "sin",
                CalcKey.Function(if (inverse) "asin" else "sin"),
                KeyStyle.Function,
            ),
            KeySpec(
                if (inverse) "cos⁻¹" else "cos",
                CalcKey.Function(if (inverse) "acos" else "cos"),
                KeyStyle.Function,
            ),
            KeySpec(
                if (inverse) "tan⁻¹" else "tan",
                CalcKey.Function(if (inverse) "atan" else "tan"),
                KeyStyle.Function,
            ),
        ),
        listOf(
            if (inverse) {
                KeySpec("x²", CalcKey.Append("^2"), KeyStyle.Function, R.string.cd_square)
            } else {
                KeySpec("xʸ", CalcKey.Operator('^'), KeyStyle.Function, R.string.cd_power)
            },
            if (inverse) {
                KeySpec("∛", CalcKey.Function("cbrt"), KeyStyle.Function, R.string.cd_cube_root)
            } else {
                KeySpec("√", CalcKey.Operand("√"), KeyStyle.Function, R.string.cd_square_root)
            },
            if (inverse) {
                KeySpec("eˣ", CalcKey.Function("exp"), KeyStyle.Function, R.string.cd_exp_function)
            } else {
                KeySpec("ln", CalcKey.Function("ln"), KeyStyle.Function, R.string.cd_natural_log)
            },
            if (inverse) {
                KeySpec("10ˣ", CalcKey.Operand("10^"), KeyStyle.Function, R.string.cd_power_of_ten)
            } else {
                KeySpec("log", CalcKey.Function("log"), KeyStyle.Function, R.string.cd_log)
            },
            KeySpec("x!", CalcKey.Factorial, KeyStyle.Function, R.string.cd_factorial),
        ),
        listOf(
            KeySpec("EXP", CalcKey.Append("E"), KeyStyle.Function, R.string.cd_exponent),
            KeySpec("|x|", CalcKey.Function("abs"), KeyStyle.Function, R.string.cd_absolute),
            KeySpec("π", CalcKey.Operand("π"), KeyStyle.Function, R.string.cd_pi),
            KeySpec("e", CalcKey.Operand("e"), KeyStyle.Function, R.string.cd_euler),
            KeySpec("ans", CalcKey.Operand("ans"), KeyStyle.Function, R.string.cd_answer),
        ),
    )
}
