package com.capyreader.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.isCompact
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsView(
    onNavigateBack: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<SettingsPanel>()
    val currentPanel = navigator.currentDestination?.contentKey

    SettingsScaffold(
        scaffoldNavigator = navigator,
        listPane = {
            SettingsList(
                selected = currentPanel,
                onNavigate = { panel ->
                    coroutineScope.launchUI {
                        navigator.navigateTo(ThreePaneScaffoldRole.Primary, panel)
                    }
                },
                onNavigateBack = onNavigateBack
            )
        },
        detailPane = {
            if (currentPanel == null && !isCompact()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CapyPlaceholder()
                }
            } else if (currentPanel != null) {
                SettingsPanelScaffold(
                    onBack = {
                        coroutineScope.launchUI {
                            navigator.navigateBack()
                        }
                    },
                    panel = currentPanel,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when (currentPanel) {
                            SettingsPanel.General -> GeneralSettingsPanel()
                            SettingsPanel.Display -> DisplaySettingsPanel()
                            SettingsPanel.Gestures -> GesturesSettingPanel()
                            SettingsPanel.Account -> AccountSettingsPanel(onRemoveAccount = onRemoveAccount)
                            SettingsPanel.About -> AboutSettingsPanel()
                        }
                    }
                }
            }
        }
    )

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launchUI {
            navigator.navigateBack()
        }
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
