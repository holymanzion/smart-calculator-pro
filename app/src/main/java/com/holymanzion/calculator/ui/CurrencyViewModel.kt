package com.holymanzion.calculator.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.holymanzion.calculator.data.CalculatorPreferences
import com.holymanzion.calculator.tools.currency.CurrencyRates
import com.holymanzion.calculator.tools.currency.RatesApi
import com.holymanzion.calculator.tools.currency.RatesResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CurrencyUiState(
    val rates: CurrencyRates = CurrencyRates.EMPTY,
    val refreshing: Boolean = false,
    /** Set when the last refresh failed; cached rates stay on screen regardless. */
    val lastRefreshFailedOffline: Boolean? = null,
)

/**
 * Owns the exchange rate cache and its refreshes.
 *
 * Offline-first on purpose: cached rates render immediately and a refresh happens
 * behind them. A failed refresh never clears what is already on screen — old rates
 * with an honest date beat an empty screen.
 */
class CurrencyViewModel(
    private val preferences: CalculatorPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencyUiState())
    val state: StateFlow<CurrencyUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cached = withContext(Dispatchers.IO) { preferences.loadRates() }
            _state.update { it.copy(rates = cached) }

            // Only reach for the network when the cache is actually due.
            if (cached.shouldRefresh(System.currentTimeMillis())) refresh()
        }
    }

    /** @param force true for an explicit user-initiated refresh, bypassing the age check. */
    fun refresh(force: Boolean = false) {
        if (_state.value.refreshing) return

        val current = _state.value.rates
        if (!force && !current.shouldRefresh(System.currentTimeMillis())) return

        _state.update { it.copy(refreshing = true, lastRefreshFailedOffline = null) }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { RatesApi.fetch() }

            when (result) {
                is RatesResult.Success -> {
                    withContext(Dispatchers.IO) {
                        preferences.saveRates(result.body, result.rates.fetchedAtMillis)
                    }
                    _state.update {
                        it.copy(
                            rates = result.rates,
                            refreshing = false,
                            lastRefreshFailedOffline = null,
                        )
                    }
                }

                is RatesResult.Failure -> _state.update {
                    it.copy(refreshing = false, lastRefreshFailedOffline = result.offline)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                CurrencyViewModel(CalculatorPreferences(application))
            }
        }
    }
}
