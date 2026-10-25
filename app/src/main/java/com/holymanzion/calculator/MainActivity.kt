package com.holymanzion.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holymanzion.calculator.data.ThemeMode
import com.holymanzion.calculator.ui.AppViewModel
import com.holymanzion.calculator.ui.Destination
import com.holymanzion.calculator.ui.SmartCalculatorApp
import com.holymanzion.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // A launcher shortcut names the screen to open. Read once, here, rather than
        // in composition: it is a property of how the activity was started, and
        // re-reading it would fight the user's own navigation after a rotation.
        val startDestination = intent?.getStringExtra(EXTRA_DESTINATION)
            ?.let { name -> Destination.entries.firstOrNull { it.name == name } }

        setContent {
            val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
            val settings by appViewModel.settings.collectAsStateWithLifecycle()

            val darkTheme = when (settings.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            CalculatorTheme(
                darkTheme = darkTheme,
                dynamicColor = settings.dynamicColor,
            ) {
                SmartCalculatorApp(
                    startDestination = startDestination,
                    settings = settings,
                    onThemeModeChange = appViewModel::onThemeModeChange,
                    onDynamicColorChange = appViewModel::onDynamicColorChange,
                    onGroupDigitsChange = appViewModel::onGroupDigitsChange,
                    onHapticFeedbackChange = appViewModel::onHapticFeedbackChange,
                    onCurrencySymbolChange = appViewModel::onCurrencySymbolChange,
                )
            }
        }
    }

    private companion object {
        /** Matches the extra name used in `res/xml/shortcuts.xml`. */
        const val EXTRA_DESTINATION = "destination"
    }
}
