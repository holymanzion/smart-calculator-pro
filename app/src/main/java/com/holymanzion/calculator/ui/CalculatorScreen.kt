package com.holymanzion.calculator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holymanzion.calculator.R
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(factory = CalculatorViewModel.Factory),
    onOpenMenu: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.copied)

    val onCopy: (String) -> Unit = { text ->
        if (text.isNotEmpty()) {
            copyToClipboard(context, text)
            // Android 13+ shows its own copy confirmation; a second one would be noise.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Landscape has the width for the scientific pad, so it is always shown.
            val landscape = maxWidth > maxHeight
            val effectiveState = if (landscape) state.copy(scientific = true) else state

            if (landscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f)) {
                        ControlBar(state, viewModel, onOpenMenu)
                        Display(
                            state = state,
                            onCopyResult = onCopy,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Keypad(
                        state = effectiveState,
                        onKey = { viewModel.handle(it) },
                        modifier = Modifier.weight(1.15f),
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    ControlBar(state, viewModel, onOpenMenu)
                    Display(
                        state = state,
                        onCopyResult = onCopy,
                        modifier = Modifier.weight(1f),
                    )
                    Keypad(
                        state = effectiveState,
                        onKey = { viewModel.handle(it) },
                        modifier = Modifier.weight(
                            if (state.scientific) SCIENTIFIC_KEYPAD_WEIGHT else BASIC_KEYPAD_WEIGHT,
                        ),
                    )
                }
            }
        }
    }

    if (state.historyVisible) {
        HistoryPanel(
            history = state.history,
            onDismiss = viewModel::onDismissHistory,
            onSelect = viewModel::onHistoryEntrySelected,
            onClear = viewModel::onClearHistory,
        )
    }
}

@Composable
private fun ControlBar(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    onOpenMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The calculator has no app bar — it needs the full height for the keypad — so
        // the drawer button lives here instead.
        IconButton(onClick = onOpenMenu) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Open menu",
            )
        }

        IconButton(onClick = viewModel::onToggleHistory) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = stringResource(R.string.cd_history),
            )
        }

        IconButton(
            onClick = viewModel::onToggleScientific,
            colors = if (state.scientific) {
                IconButtonDefaults.filledIconButtonColors()
            } else {
                IconButtonDefaults.iconButtonColors()
            },
        ) {
            Icon(
                imageVector = Icons.Filled.Functions,
                contentDescription = stringResource(R.string.cd_scientific),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = viewModel::onBackspace) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.cd_backspace),
            )
        }
    }
}

/** Routes a keypad press to the matching view-model call. */
private fun CalculatorViewModel.handle(key: CalcKey) {
    when (key) {
        is CalcKey.Digit -> onDigit(key.value)
        is CalcKey.Operator -> onOperator(key.symbol)
        is CalcKey.Function -> onFunction(key.name)
        is CalcKey.Operand -> onInsertOperand(key.text)
        is CalcKey.Append -> onAppend(key.text)
        CalcKey.Dot -> onDot()
        CalcKey.Equals -> onEquals()
        CalcKey.Clear -> onClear()
        CalcKey.Backspace -> onBackspace()
        CalcKey.Parenthesis -> onParenthesis()
        CalcKey.Percent -> onPostfix('%')
        CalcKey.Factorial -> onPostfix('!')
        CalcKey.ToggleSign -> onToggleSign()
        CalcKey.ToggleSecond -> onToggleSecondFunction()
        CalcKey.ToggleAngle -> onToggleAngleMode()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("calculation", text))
}

/** The keypad takes the majority of the screen; the display gets the rest. */
private const val BASIC_KEYPAD_WEIGHT = 2.1f
private const val SCIENTIFIC_KEYPAD_WEIGHT = 3.3f
