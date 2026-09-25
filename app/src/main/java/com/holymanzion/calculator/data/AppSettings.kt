package com.holymanzion.calculator.data

/** How the app picks between the light and dark colour schemes. */
enum class ThemeMode(val label: String) {
    System("Follow system"),
    Light("Light"),
    Dark("Dark"),
}

/** User preferences that apply across every screen. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    /** Material You wallpaper colours, where the platform supports them. */
    val dynamicColor: Boolean = true,
    /** Thousands separators in results. */
    val groupDigits: Boolean = true,
    val hapticFeedback: Boolean = true,
    /**
     * Prefix for money results in the finance tools.
     *
     * Null means "follow the device locale" and is resolved at render time, so the
     * app tracks a locale change instead of freezing whatever was current at install.
     * The empty string is a deliberate choice of no symbol at all.
     */
    val currencySymbol: String? = null,
)
