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
import com.holymanzion.calculator.ui.SmartCalculatorApp
import com.holymanzion.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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
}
