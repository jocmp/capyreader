package com.capyreader.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.isCompact
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsView(
    onNavigateBack: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    val compact = isCompact()
    val navigator = rememberListDetailPaneScaffoldNavigator<SettingsPanel>(
        isDestinationHistoryAware = false
    )
    val currentPanel = navigator.currentDestination?.content
    val (isInitialized, setInitialized) = rememberSaveable { mutableStateOf(false) }

    SettingsScaffold(
        scaffoldNavigator = navigator,
        listPane = {
            SettingsList(
                selected = currentPanel,
                onNavigate = { panel ->
                    navigator.navigateTo(ThreePaneScaffoldRole.Primary, panel)
                },
                onNavigateBack = onNavigateBack
            )
        },
        detailPane = {
            currentPanel?.let { panel ->
                SettingsPanelScaffold(
                    onBack = {
                        navigator.navigateBack()
                    },
                    title = panel.title,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when (panel) {
                            SettingsPanel.General -> GeneralSettingsPanel()
                            SettingsPanel.Display -> DisplaySettingsPanel()
                            SettingsPanel.Account -> AccountSettingsPanel(onRemoveAccount = onRemoveAccount)
                            SettingsPanel.About -> AboutSettingsPanel()
                        }
                    }
                }
            }
        }
    )

    BackHandler(currentPanel == SettingsPanel.General) {
        onNavigateBack()
    }

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    LaunchedEffect(isInitialized, navigator.canNavigateBack()) {
        if (isInitialized || navigator.canNavigateBack()) {
            return@LaunchedEffect
        }

        if (!compact) {
            navigator.navigateTo(
                pane = ThreePaneScaffoldRole.Secondary,
                SettingsPanel.General
            )
        }

        setInitialized(true)
    }
}

@Preview
@Composable
fun AccountSettingsViewPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        SettingsView(
            onNavigateBack = {},
            onRemoveAccount = {}
        )
    }
}
