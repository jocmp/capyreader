package com.capyreader.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.provideLinkOpener
import com.capyreader.app.ui.settings.panels.AboutSettingsPanel
import com.capyreader.app.ui.settings.panels.AccountSettingsPanel
import com.capyreader.app.ui.settings.panels.DisplaySettingsPanel
import com.capyreader.app.ui.settings.panels.GeneralSettingsPanel
import com.capyreader.app.ui.settings.panels.GesturesSettingPanel
import com.capyreader.app.ui.settings.panels.NotificationsSettingsPanel
import com.capyreader.app.ui.settings.panels.SettingsPanel
import com.capyreader.app.ui.settings.panels.SettingsViewModel
import com.jocmp.capy.common.launchUI
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsView(
    viewModel: SettingsViewModel = koinInject(),
    onNavigateBack: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<SettingsPanel>()
    val currentPanel = navigator.currentDestination?.contentKey
    val feeds by viewModel.feeds.collectAsStateWithLifecycle(emptyList())

    val navigateToPanel = { panel: SettingsPanel ->
        coroutineScope.launchUI {
            navigator.navigateTo(ThreePaneScaffoldRole.Primary, panel)
        }
    }

    val navigateBack = {
        coroutineScope.launchUI {
            navigator.navigateBack(BackNavigationBehavior.PopLatest)
        }
    }

    CompositionLocalProvider(
        LocalLinkOpener provides provideLinkOpener(context)
    ) {
        SettingsScaffold(
            scaffoldNavigator = navigator,
            listPane = {
                SettingsList(
                    selected = currentPanel,
                    onNavigate = { navigateToPanel(it) },
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
                        panel = currentPanel,
                        onBack = {
                            navigateBack()
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when (currentPanel) {
                                SettingsPanel.General -> GeneralSettingsPanel(
                                    onNavigateToNotifications = {
                                        navigateToPanel(SettingsPanel.Notifications)
                                    }
                                )

                                SettingsPanel.Notifications -> NotificationsSettingsPanel(
                                    onSelectNone = viewModel::deselectAllFeedNotifications,
                                    onSelectAll = viewModel::selectAllFeedNotifications,
                                    onToggleNotifications = viewModel::toggleNotifications,
                                    feeds = feeds,
                                )

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
    }

    BackHandler(navigator.canNavigateBack(BackNavigationBehavior.PopLatest)) {
        navigateBack()
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
