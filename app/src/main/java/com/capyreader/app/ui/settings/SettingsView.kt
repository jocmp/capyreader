package com.capyreader.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.BuildConfig
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.CrashReporting
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.isSinglePane
import com.capyreader.app.ui.provideLinkOpener
import com.capyreader.app.ui.settings.panels.AboutSettingsPanel
import com.capyreader.app.ui.settings.panels.NotificationsSettingsPanel
import com.capyreader.app.ui.settings.panels.SettingsPanel
import com.capyreader.app.ui.settings.panels.SettingsViewModel
import com.capyreader.app.ui.settings.panels.UnreadBadgesSettingsPanel
import com.capyreader.app.ui.settings.registry.RegistryPanel
import com.capyreader.app.ui.settings.registry.Setting
import com.capyreader.app.ui.settings.registry.SettingsEnvironment
import com.jocmp.capy.common.launchUI
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

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
    val savedSearches by viewModel.savedSearches.collectAsStateWithLifecycle(emptyList())

    var query by rememberSaveable { mutableStateOf("") }
    var highlighted by remember { mutableStateOf<Setting?>(null) }
    val environment = remember(viewModel.source) {
        SettingsEnvironment(
            source = viewModel.source,
            crashReporting = CrashReporting.isAvailable,
            debug = BuildConfig.DEBUG,
        )
    }

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
                    environment = environment,
                    query = query,
                    onQueryChange = { query = it },
                    onNavigate = { navigateToPanel(it) },
                    onSelectResult = { result ->
                        coroutineScope.launchUI {
                            navigator.navigateTo(ThreePaneScaffoldRole.Primary, result.panel)
                            highlighted = result.setting
                        }
                    },
                    onNavigateBack = onNavigateBack
                )
            },
            detailPane = {
                val panel = currentPanel

                if (panel == null && !isSinglePane()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CapyPlaceholder()
                    }
                } else if (panel != null) {
                    SettingsPanelScaffold(
                        panel = panel,
                        onBack = {
                            navigateBack()
                        },
                    ) {
                        when (panel) {
                            SettingsPanel.Notifications -> NotificationsSettingsPanel(
                                onSelectNone = viewModel::deselectAllFeedNotifications,
                                onSelectAll = viewModel::selectAllFeedNotifications,
                                onToggleNotifications = viewModel::toggleNotifications,
                                feeds = feeds,
                            )

                            SettingsPanel.About -> AboutSettingsPanel()

                            SettingsPanel.Account,
                            SettingsPanel.ArticleList,
                            SettingsPanel.Reader,
                            SettingsPanel.Display,
                            SettingsPanel.Advanced -> RegistryPanel(
                                panel = panel,
                                environment = environment,
                                highlighted = highlighted,
                                onHighlightShown = { highlighted = null },
                                onRemoveAccount = onRemoveAccount,
                                onNavigate = { navigateToPanel(it) },
                            )

                            SettingsPanel.UnreadBadges -> UnreadBadgesSettingsPanel(
                                badgeStyle = viewModel.badgeStyle,
                                updateBadgeStyle = viewModel::updateBadgeStyle,
                                source = viewModel.source,
                                feeds = feeds,
                                savedSearches = savedSearches,
                                onSelectAll = viewModel::selectAllBadges,
                                onSelectNone = viewModel::selectNoBadges,
                                onToggleFeed = viewModel::toggleFeedUnreadBadge,
                                onToggleSavedSearch = viewModel::toggleSavedSearchUnreadBadge,
                            )
                        }
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun AccountSettingsViewPreview() {
    val context = LocalContext.current

    KoinApplication(
        configuration = koinConfiguration {
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
