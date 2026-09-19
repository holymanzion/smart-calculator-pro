package com.holymanzion.calculator.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holymanzion.calculator.R
import com.holymanzion.calculator.core.CalcError
import com.holymanzion.calculator.ui.theme.ResultTextStyle

/**
 * The expression and result area.
 *
 * Three lines, top to bottom: the expression that produced the current result (only
 * after `=`), the expression being edited, and either the live preview or an error.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Display(
    state: CalculatorUiState,
    onCopyResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    // Keep the caret end of a long expression in view as it grows.
    LaunchedEffect(state.displayExpression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        if (state.justEvaluated && state.evaluatedExpression.isNotEmpty()) {
            Text(
                text = "${state.evaluatedExpression} =",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                textAlign = TextAlign.End,
            )
        }

        val expressionText = state.displayExpression.ifEmpty { "0" }
        Text(
            text = expressionText,
            fontSize = expressionFontSize(expressionText.length),
            lineHeight = expressionFontSize(expressionText.length) * 1.2f,
            fontWeight = FontWeight.Light,
            color = if (state.displayExpression.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onLongClick = { onCopyResult(state.expression) },
                    onClick = {},
                ),
        )

        val secondary = secondaryLine(state)
        if (secondary != null) {
            Text(
                text = secondary.text,
                style = ResultTextStyle,
                color = if (secondary.isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .horizontalScroll(rememberScrollState())
                    .semantics {
                        contentDescription = secondary.text
                    },
            )
        }
    }
}

private data class SecondaryLine(val text: String, val isError: Boolean)

@Composable
private fun secondaryLine(state: CalculatorUiState): SecondaryLine? = when {
    state.error != null -> SecondaryLine(stringResource(state.error.messageRes()), isError = true)
    state.preview.isNotEmpty() -> SecondaryLine("= ${state.preview}", isError = false)
    else -> null
}

/**
 * Shrinks the expression as it lengthens so more of it fits before the row has to
 * scroll. Overflow still scrolls; this just delays the point at which it starts.
 */
private fun expressionFontSize(length: Int) = when {
    length <= 10 -> 52.sp
    length <= 15 -> 42.sp
    length <= 22 -> 34.sp
    else -> 28.sp
}

private fun CalcError.messageRes(): Int = when (this) {
    CalcError.Syntax, CalcError.Empty -> R.string.error_syntax
    CalcError.DivisionByZero -> R.string.error_division_by_zero
    CalcError.Domain -> R.string.error_domain
    CalcError.Overflow -> R.string.error_overflow
    CalcError.UnbalancedParens -> R.string.error_parens
}
