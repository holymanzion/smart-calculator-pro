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
)
