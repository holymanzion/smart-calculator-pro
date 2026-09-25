package com.holymanzion.calculator.ui

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.holymanzion.calculator.BuildConfig
import com.holymanzion.calculator.data.AppSettings
import com.holymanzion.calculator.data.ThemeMode
import com.holymanzion.calculator.ui.components.ChipSelector
import com.holymanzion.calculator.ui.components.defaultCurrencySymbol

/** A short list of widely used symbols, offered alongside the device's own. */
private val CURRENCY_SYMBOLS = listOf("$", "€", "£", "₵", "₦", "₹", "¥", "R", "KSh")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    historyCount: Int,
    onOpenMenu: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onGroupDigitsChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onCurrencySymbolChange: (String?) -> Unit,
    onClearHistory: () -> Unit,
    onOpenLegal: (LegalDocument) -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            SettingsSection("Appearance") {
                SettingsLabel("Theme")
                ChipSelector(
                    options = ThemeMode.entries.map { it.label },
                    selectedIndex = settings.themeMode.ordinal,
                    onSelect = { onThemeModeChange(ThemeMode.entries[it]) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                // Material You only exists from Android 12; offering the switch on
                // older versions would be a control that does nothing.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsSwitch(
                        title = "Material You colours",
                        subtitle = "Match the app to your wallpaper",
                        checked = settings.dynamicColor,
                        onCheckedChange = onDynamicColorChange,
                    )
                }
            }

            SettingsSection("Calculator") {
                SettingsSwitch(
                    title = "Thousands separators",
                    subtitle = "Show 1,000,000 instead of 1000000",
                    checked = settings.groupDigits,
                    onCheckedChange = onGroupDigitsChange,
                )
                SettingsSwitch(
                    title = "Haptic feedback",
                    subtitle = "Vibrate on key press",
                    checked = settings.hapticFeedback,
                    onCheckedChange = onHapticFeedbackChange,
                )
            }

            SettingsSection("Currency") {
                SettingsLabel("Symbol shown by the money tools")
                val auto = remember { defaultCurrencySymbol() }
                // null = follow the device locale, "" = no symbol; the rest are literals.
                val choices = remember(auto) {
                    listOf<String?>(null, "") + CURRENCY_SYMBOLS.filter { it != auto }
                }
                val labels = remember(auto, choices) {
                    choices.map {
                        when (it) {
                            null -> if (auto.isEmpty()) "Automatic" else "Automatic ($auto)"
                            "" -> "None"
                            else -> it
                        }
                    }
                }
                ChipSelector(
                    options = labels,
                    selectedIndex = choices.indexOf(settings.currencySymbol),
                    onSelect = { onCurrencySymbolChange(choices[it]) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            SettingsSection("Data") {
                SettingsRow(
                    title = "Clear history",
                    subtitle = if (historyCount == 0) {
                        "Nothing saved"
                    } else {
                        "$historyCount saved ${if (historyCount == 1) "calculation" else "calculations"}"
                    },
                    icon = Icons.Filled.DeleteSweep,
                    enabled = historyCount > 0,
                    onClick = { confirmClear = true },
                )
            }

            SettingsSection("Legal") {
                SettingsRow(
                    title = "Privacy Policy",
                    subtitle = "How your data is handled",
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    onClick = { onOpenLegal(LegalDocument.PrivacyPolicy) },
                )
                SettingsRow(
                    title = "Terms & Conditions",
                    subtitle = "Terms of use for this app",
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    onClick = { onOpenLegal(LegalDocument.Terms) },
                )
            }

            SettingsSection("About") {
                SettingsRow(
                    title = "Version",
                    subtitle = "${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                    onClick = null,
                )
                SettingsRow(
                    title = "Package",
                    subtitle = BuildConfig.APPLICATION_ID,
                    onClick = null,
                )
            }

            Text(
                text = "Smart Calculator Pro works entirely on your device. " +
                    "It has no internet permission and collects nothing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear history?") },
            text = { Text("This removes all saved calculations. It cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        confirmClear = false
                    },
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
        )
        content()
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun SettingsLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
) {
    val contentColour = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = contentColour)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColour)
        }
    }
}
