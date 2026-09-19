package com.holymanzion.calculator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holymanzion.calculator.data.AppSettings
import com.holymanzion.calculator.data.ThemeMode
import com.holymanzion.calculator.ui.tools.BmiScreen
import com.holymanzion.calculator.ui.tools.DateScreen
import com.holymanzion.calculator.ui.tools.DiscountScreen
import com.holymanzion.calculator.ui.tools.LoanScreen
import com.holymanzion.calculator.ui.tools.PercentageScreen
import com.holymanzion.calculator.ui.tools.PriceScreen
import com.holymanzion.calculator.ui.tools.UnitConverterScreen
import kotlinx.coroutines.launch

/**
 * Root of the app: a navigation drawer over a flat set of tool screens.
 *
 * Navigation is a single [Destination] held in saveable state rather than a navigation
 * graph. With no arguments to pass and no nested routes, a library would add a
 * dependency and a layer of indirection without buying anything.
 */
@Composable
fun SmartCalculatorApp(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onGroupDigitsChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var destinationName by rememberSaveable { mutableStateOf(Destination.Calculator.name) }
    val destination = Destination.valueOf(destinationName)

    var legalDocumentName by rememberSaveable { mutableStateOf<String?>(null) }
    val legalDocument = legalDocumentName?.let { LegalDocument.valueOf(it) }

    // Shares the activity's ViewModelStore with CalculatorScreen, so this is the same
    // instance that screen uses — history and settings stay in step.
    val calculatorViewModel: CalculatorViewModel =
        viewModel(factory = CalculatorViewModel.Factory)
    val calculatorState by calculatorViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(settings.groupDigits) {
        calculatorViewModel.setGroupDigits(settings.groupDigits)
    }

    fun navigateTo(target: Destination) {
        destinationName = target.name
        scope.launch { drawerState.close() }
    }

    // Back should unwind the UI one layer at a time before leaving the app.
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = drawerState.isClosed && destination != Destination.Calculator) {
        destinationName = Destination.Calculator.name
    }

    CompositionLocalProvider(LocalHapticsEnabled provides settings.hapticFeedback) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Smart Calculator Pro",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 28.dp, top = 24.dp, bottom = 4.dp),
                        )
                        Text(
                            text = "Everything offline, nothing collected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 28.dp, bottom = 16.dp),
                        )

                        Destination.tools.forEach { target ->
                            DrawerEntry(target, destination == target) { navigateTo(target) }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )

                        DrawerEntry(
                            destination = Destination.Settings,
                            selected = destination == Destination.Settings,
                            onClick = { navigateTo(Destination.Settings) },
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }
            },
        ) {
            val openMenu: () -> Unit = { scope.launch { drawerState.open() } }

            when (destination) {
                Destination.Calculator -> CalculatorScreen(
                    viewModel = calculatorViewModel,
                    onOpenMenu = openMenu,
                )

                Destination.UnitConverter -> UnitConverterScreen(onOpenMenu = openMenu)
                Destination.Discount -> DiscountScreen(onOpenMenu = openMenu)
                Destination.Price -> PriceScreen(onOpenMenu = openMenu)
                Destination.Loan -> LoanScreen(onOpenMenu = openMenu)
                Destination.Percentage -> PercentageScreen(onOpenMenu = openMenu)
                Destination.DateTools -> DateScreen(onOpenMenu = openMenu)
                Destination.Bmi -> BmiScreen(onOpenMenu = openMenu)

                Destination.Settings -> SettingsScreen(
                    settings = settings,
                    historyCount = calculatorState.history.size,
                    onOpenMenu = openMenu,
                    onThemeModeChange = onThemeModeChange,
                    onDynamicColorChange = onDynamicColorChange,
                    onGroupDigitsChange = onGroupDigitsChange,
                    onHapticFeedbackChange = onHapticFeedbackChange,
                    onClearHistory = calculatorViewModel::onClearHistory,
                    onOpenLegal = { legalDocumentName = it.name },
                )
            }
        }
    }

    // Drawn last so it covers the drawer scrim as well as the screen behind it.
    if (legalDocument != null) {
        LegalScreen(
            document = legalDocument,
            onBack = { legalDocumentName = null },
        )
    }
}

@Composable
private fun DrawerEntry(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        icon = { Icon(destination.icon, contentDescription = null) },
        label = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(destination.title)
                Text(
                    text = destination.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}
