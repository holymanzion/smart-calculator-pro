package com.holymanzion.calculator.data

import android.content.Context
import androidx.core.content.edit
import com.holymanzion.calculator.core.AngleMode
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Persistence for history and the two sticky UI toggles.
 *
 * History is small and bounded, so SharedPreferences plus a JSON blob is enough; it
 * avoids pulling in a database or a serialization plugin for a handful of rows.
 */
class CalculatorPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- history ---------------------------------------------------------

    fun loadHistory(): List<HistoryEntry> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        HistoryEntry(
                            expression = item.optString(FIELD_EXPRESSION),
                            result = item.optString(FIELD_RESULT),
                            rawResult = item.optString(FIELD_RAW_RESULT),
                            timestamp = item.optLong(FIELD_TIMESTAMP),
                        ),
                    )
                }
            }
        } catch (_: JSONException) {
            // A corrupt blob should cost the user their history, not the app's launch.
            emptyList()
        }
    }

    fun saveHistory(entries: List<HistoryEntry>) {
        val array = JSONArray()
        entries.take(MAX_ENTRIES).forEach { entry ->
            array.put(
                JSONObject()
                    .put(FIELD_EXPRESSION, entry.expression)
                    .put(FIELD_RESULT, entry.result)
                    .put(FIELD_RAW_RESULT, entry.rawResult)
                    .put(FIELD_TIMESTAMP, entry.timestamp),
            )
        }
        prefs.edit { putString(KEY_HISTORY, array.toString()) }
    }

    // ---- settings --------------------------------------------------------

    fun loadAngleMode(): AngleMode =
        if (prefs.getBoolean(KEY_RADIANS, false)) AngleMode.Radians else AngleMode.Degrees

    fun saveAngleMode(mode: AngleMode) {
        prefs.edit { putBoolean(KEY_RADIANS, mode == AngleMode.Radians) }
    }

    fun loadScientific(): Boolean = prefs.getBoolean(KEY_SCIENTIFIC, false)

    fun saveScientific(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SCIENTIFIC, enabled) }
    }

    // ---- app-wide settings -----------------------------------------------

    fun loadAppSettings(): AppSettings {
        val defaults = AppSettings()
        val storedTheme = prefs.getString(KEY_THEME_MODE, null)
        return AppSettings(
            themeMode = ThemeMode.entries.firstOrNull { it.name == storedTheme } ?: defaults.themeMode,
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, defaults.dynamicColor),
            groupDigits = prefs.getBoolean(KEY_GROUP_DIGITS, defaults.groupDigits),
            hapticFeedback = prefs.getBoolean(KEY_HAPTICS, defaults.hapticFeedback),
        )
    }

    fun saveAppSettings(settings: AppSettings) {
        prefs.edit {
            putString(KEY_THEME_MODE, settings.themeMode.name)
            putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            putBoolean(KEY_GROUP_DIGITS, settings.groupDigits)
            putBoolean(KEY_HAPTICS, settings.hapticFeedback)
        }
    }

    companion object {
        /** Referenced by name in the backup rules, so keep the two in step. */
        const val PREFS_NAME = "calculator_prefs"

        /** Plenty for scrollback while keeping the blob small. */
        const val MAX_ENTRIES = 100

        private const val KEY_HISTORY = "history"
        private const val KEY_RADIANS = "angle_radians"
        private const val KEY_SCIENTIFIC = "scientific_keypad"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_GROUP_DIGITS = "group_digits"
        private const val KEY_HAPTICS = "haptics"

        private const val FIELD_EXPRESSION = "expression"
        private const val FIELD_RESULT = "result"
        private const val FIELD_RAW_RESULT = "raw"
        private const val FIELD_TIMESTAMP = "ts"
    }
}
