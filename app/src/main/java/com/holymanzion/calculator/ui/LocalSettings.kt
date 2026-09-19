package com.holymanzion.calculator.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether key presses vibrate.
 *
 * Passed through a composition local rather than threaded down as a parameter: only the
 * leaf button reads it, and every layer in between would otherwise have to carry it.
 */
val LocalHapticsEnabled = staticCompositionLocalOf { true }
