package com.holymanzion.calculator.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.holymanzion.calculator.data.AppSettings
import com.holymanzion.calculator.data.CalculatorPreferences
import com.holymanzion.calculator.data.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Holds preferences that outlive any single screen, so the theme can be applied at the
 * activity root before the navigation graph is composed.
 */
class AppViewModel(
    private val preferences: CalculatorPreferences,
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /**
     * True until preferences have been read back from disk. The activity holds the
     * splash screen until then so the app never flashes the wrong theme.
     */
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            val restored = withContext(Dispatchers.IO) { preferences.loadAppSettings() }
            _settings.value = restored
            _loading.value = false
        }
    }

    fun onThemeModeChange(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun onDynamicColorChange(enabled: Boolean) = update { it.copy(dynamicColor = enabled) }

    fun onGroupDigitsChange(enabled: Boolean) = update { it.copy(groupDigits = enabled) }

    fun onHapticFeedbackChange(enabled: Boolean) = update { it.copy(hapticFeedback = enabled) }

    /** @param symbol null follows the device locale; empty shows no symbol at all. */
    fun onCurrencySymbolChange(symbol: String?) = update { it.copy(currencySymbol = symbol) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        viewModelScope.launch {
            withContext(Dispatchers.IO) { preferences.saveAppSettings(updated) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                AppViewModel(CalculatorPreferences(application))
            }
        }
    }
}
