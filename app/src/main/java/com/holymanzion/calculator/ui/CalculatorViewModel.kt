package com.holymanzion.calculator.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.holymanzion.calculator.core.AngleMode
import com.holymanzion.calculator.core.CalcError
import com.holymanzion.calculator.core.CalcResult
import com.holymanzion.calculator.core.Calculator
import com.holymanzion.calculator.core.ExpressionFormatter
import com.holymanzion.calculator.core.NumberFormatter
import com.holymanzion.calculator.data.CalculatorPreferences
import com.holymanzion.calculator.data.HistoryEntry
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CalculatorUiState(
    /** Raw editable text. Never contains thousands separators. */
    val expression: String = "",
    /** [expression] with separators added, for rendering. */
    val displayExpression: String = "",
    /** Live result of a complete expression, or empty when there is nothing to show. */
    val preview: String = "",
    /** The expression that produced the current result, shown above it after `=`. */
    val evaluatedExpression: String = "",
    val error: CalcError? = null,
    val angleMode: AngleMode = AngleMode.Degrees,
    val scientific: Boolean = false,
    val secondFunction: Boolean = false,
    val history: List<HistoryEntry> = emptyList(),
    val historyVisible: Boolean = false,
    /** True while the display holds a result rather than something being typed. */
    val justEvaluated: Boolean = false,
)

class CalculatorViewModel(
    private val preferences: CalculatorPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    /** Backing value for the `ans` identifier. */
    private var ans: BigDecimal = BigDecimal.ZERO

    /** Mirrors the app-wide setting; see [setGroupDigits]. */
    private var groupDigits: Boolean = true

    init {
        viewModelScope.launch {
            val restored = withContext(Dispatchers.IO) {
                Triple(
                    preferences.loadHistory(),
                    preferences.loadAngleMode(),
                    preferences.loadScientific(),
                )
            }
            _state.update {
                it.copy(
                    history = restored.first,
                    angleMode = restored.second,
                    scientific = restored.third,
                )
            }
        }
    }

    // ---- entry -----------------------------------------------------------

    fun onDigit(digit: String) = updateExpression(operandBase() + digit)

    /** Inserts text that starts a new operand, such as `π` or `10^`. */
    fun onInsertOperand(text: String) = updateExpression(operandBase() + text)

    /** Appends text that only makes sense after an existing value, such as `^2` or `E`. */
    fun onAppend(text: String) {
        val base = _state.value.expression
        if (base.isEmpty() || base.last() in OPERATORS || base.last() == '(') return
        updateExpression(base + text)
    }

    fun onFunction(name: String) = updateExpression(operandBase() + name + "(")

    fun onDot() {
        val base = operandBase()
        val tail = base.takeLastWhile { it.isDigit() || it == '.' }
        // One decimal point per number.
        if (tail.contains('.')) return
        // `.5` reads better as `0.5`.
        updateExpression(base + if (tail.isEmpty()) "0." else ".")
    }

    fun onOperator(symbol: Char) {
        val base = _state.value.expression
        if (base.isEmpty()) {
            if (symbol == MINUS) updateExpression(MINUS.toString())
            return
        }

        val last = base.last()
        if (last == '(') {
            // Only a sign can directly follow an opening bracket.
            if (symbol == MINUS) updateExpression(base + symbol)
            return
        }

        if (last in OPERATORS) {
            // `2^−3` and `2×−3` are real expressions, so a minus after another
            // operator appends. Anything else means the user is correcting
            // themselves, so replace the whole operator run.
            if (symbol == MINUS && last != MINUS) {
                updateExpression(base + symbol)
            } else {
                val trimmed = base.dropLastWhile { it in OPERATORS }
                if (trimmed.isEmpty()) {
                    if (symbol == MINUS) updateExpression(MINUS.toString())
                } else {
                    updateExpression(trimmed + symbol)
                }
            }
            return
        }

        updateExpression(base + symbol)
    }

    /** One key that opens a bracket or closes the innermost open one, whichever fits. */
    fun onParenthesis() {
        val base = operandBase()
        val unclosed = base.count { it == '(' } - base.count { it == ')' }
        val last = base.lastOrNull()
        val canClose = unclosed > 0 && last != null && last != '(' && last !in OPERATORS
        updateExpression(base + if (canClose) ')' else '(')
    }

    fun onPostfix(symbol: Char) {
        val base = _state.value.expression
        if (base.isEmpty() || base.last() in OPERATORS || base.last() == '(') return
        updateExpression(base + symbol)
    }

    /** Flips the sign of the number currently being typed. */
    fun onToggleSign() {
        val base = _state.value.expression
        if (base.isEmpty()) {
            updateExpression(MINUS.toString())
            return
        }

        var start = base.length
        while (start > 0 && (base[start - 1].isDigit() || base[start - 1] == '.')) start--

        if (start == base.length) {
            // Not on a number; a sign is only meaningful in an operand position.
            val last = base.last()
            if (last in OPERATORS || last == '(') updateExpression(base + MINUS)
            return
        }

        val precededByMinus = start > 0 && base[start - 1] == MINUS
        // Only strip the minus if it is acting as a sign rather than as subtraction.
        val minusIsSign = precededByMinus &&
            (start == 1 || base[start - 2] in OPERATORS || base[start - 2] == '(')

        if (minusIsSign) {
            updateExpression(base.removeRange(start - 1, start))
        } else {
            updateExpression(base.substring(0, start) + MINUS + base.substring(start))
        }
    }

    fun onBackspace() {
        val base = _state.value.expression
        if (base.isEmpty()) return
        // Delete `sin(` as one unit instead of leaving stranded letters behind.
        val token = FUNCTION_TOKENS.firstOrNull { base.endsWith(it) }
        updateExpression(base.dropLast(token?.length ?: 1))
    }

    fun onClear() {
        _state.update {
            it.copy(
                expression = "",
                displayExpression = "",
                preview = "",
                evaluatedExpression = "",
                error = null,
                justEvaluated = false,
            )
        }
    }

    fun onEquals() {
        val current = _state.value.expression
        if (current.isBlank()) return

        // Closing brackets on the user's behalf is expected behaviour on a calculator.
        val unclosed = (current.count { it == '(' } - current.count { it == ')' }).coerceAtLeast(0)
        val balanced = current + ")".repeat(unclosed)

        when (val result = Calculator.evaluate(balanced, _state.value.angleMode, ans)) {
            is CalcResult.Success -> {
                ans = result.value
                val raw = NumberFormatter.toExpressionText(result.value)
                val entry = HistoryEntry(
                    expression = ExpressionFormatter.pretty(balanced, groupDigits),
                    result = NumberFormatter.format(result.value, groupDigits),
                    rawResult = raw,
                    timestamp = System.currentTimeMillis(),
                )
                val history = (listOf(entry) + _state.value.history)
                    .take(CalculatorPreferences.MAX_ENTRIES)

                _state.update {
                    it.copy(
                        expression = raw,
                        displayExpression = ExpressionFormatter.pretty(raw, groupDigits),
                        preview = "",
                        evaluatedExpression = entry.expression,
                        error = null,
                        justEvaluated = true,
                        history = history,
                    )
                }
                viewModelScope.launch(Dispatchers.IO) { preferences.saveHistory(history) }
            }

            is CalcResult.Failure -> {
                // An empty expression is not worth an error message.
                if (result.error != CalcError.Empty) {
                    _state.update { it.copy(error = result.error) }
                }
            }
        }
    }

    // ---- toggles ---------------------------------------------------------

    fun onToggleAngleMode() {
        val mode = _state.value.angleMode.toggled()
        _state.update { it.copy(angleMode = mode) }
        // Trig results change with the mode, so the preview has to be recomputed.
        refreshPreview()
        viewModelScope.launch(Dispatchers.IO) { preferences.saveAngleMode(mode) }
    }

    fun onToggleScientific() {
        val enabled = !_state.value.scientific
        _state.update { it.copy(scientific = enabled) }
        viewModelScope.launch(Dispatchers.IO) { preferences.saveScientific(enabled) }
    }

    fun onToggleSecondFunction() {
        _state.update { it.copy(secondFunction = !it.secondFunction) }
    }

    fun onToggleHistory() {
        _state.update { it.copy(historyVisible = !it.historyVisible) }
    }

    fun onDismissHistory() {
        _state.update { it.copy(historyVisible = false) }
    }

    fun onClearHistory() {
        _state.update { it.copy(history = emptyList()) }
        viewModelScope.launch(Dispatchers.IO) { preferences.saveHistory(emptyList()) }
    }

    /** Pulls a past result back into the expression. */
    fun onHistoryEntrySelected(entry: HistoryEntry) {
        updateExpression(operandBase() + entry.rawResult)
        _state.update { it.copy(historyVisible = false) }
    }

    // ---- internals -------------------------------------------------------

    /**
     * The text a new operand should be appended to.
     *
     * After `=` the display holds a result. Typing a digit means starting over, but
     * typing an operator means continuing from that result, which is why only the
     * operand-entry paths call this.
     */
    private fun operandBase(): String =
        if (_state.value.justEvaluated) "" else _state.value.expression

    private fun updateExpression(expression: String) {
        _state.update {
            it.copy(
                expression = expression,
                displayExpression = ExpressionFormatter.pretty(expression, groupDigits),
                preview = computePreview(expression),
                evaluatedExpression = "",
                error = null,
                justEvaluated = false,
            )
        }
    }

    private fun refreshPreview() {
        _state.update { it.copy(preview = computePreview(it.expression)) }
    }

    /**
     * The running result shown under the expression.
     *
     * Returns empty rather than an error for half-typed input, since seeing "Error"
     * flash while typing would be noise.
     */
    private fun computePreview(expression: String): String {
        if (expression.isBlank()) return ""
        // A bare number is its own result; showing it twice adds nothing.
        val hasOperation = expression.any { it in OPERATION_MARKERS || it.isLetter() }
        if (!hasOperation) return ""

        val preview = Calculator.preview(expression, _state.value.angleMode, ans, groupDigits)
            ?: return ""
        return if (preview == ExpressionFormatter.pretty(expression, groupDigits)) "" else preview
    }

    /**
     * Applies the thousands-separator preference.
     *
     * Only affects rendering, so the stored expression is untouched and the visible
     * strings are simply rebuilt from it.
     */
    fun setGroupDigits(enabled: Boolean) {
        if (groupDigits == enabled) return
        groupDigits = enabled
        _state.update {
            it.copy(
                displayExpression = ExpressionFormatter.pretty(it.expression, enabled),
                preview = computePreview(it.expression),
            )
        }
    }

    companion object {
        /** U+2212, the typographic minus the keypad emits. */
        const val MINUS: Char = '−'
        const val TIMES: Char = '×'
        const val DIVIDE: Char = '÷'
        const val PLUS: Char = '+'
        const val POWER: Char = '^'

        private const val OPERATORS = "+−×÷^"
        private const val OPERATION_MARKERS = "+−×÷^%!(√"

        /** Longest first, so `asin(` is matched before `sin(`. */
        private val FUNCTION_TOKENS = listOf(
            "floor(", "round(",
            "asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(",
            "sqrt(", "cbrt(", "log2(", "ceil(",
            "sin(", "cos(", "tan(", "abs(", "exp(", "log(",
            "ln(",
        )

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as Application
                CalculatorViewModel(CalculatorPreferences(application))
            }
        }
    }
}
