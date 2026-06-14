package com.capyreader.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.accounts.AddAccountScreen
import com.capyreader.app.ui.accounts.LoginScreen
import com.capyreader.app.ui.articles.ArticleDetailScreen
import com.capyreader.app.ui.articles.ArticleScreen
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.settings.SettingsScreen
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.unloadAccountModules
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App(
    startDestination: Route,
    appPreferences: AppPreferences,
    pendingArticleID: String? = null,
    onPendingArticleSelected: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(startDestination)

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    CapyTheme(appPreferences) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                // Scope a ViewModelStore per NavEntry so each route instance gets its own
                // ViewModel (Login per source, ArticleDetail per article id).
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                sceneStrategies = listOf(listDetailStrategy),
                entryProvider = entryProvider {
                    entry<Route.AddAccount> {
                        AddAccountScreen(
                            onAddSuccess = { backStack.resetToArticles(appPreferences) },
                            onNavigateToLogin = { source -> backStack.add(Route.Login(source)) }
                        )
                    }
                    entry<Route.Login> { key ->
                        LoginScreen(
                            viewModel = koinViewModel { parametersOf(key.source) },
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccess = { backStack.resetToArticles(appPreferences) },
                        )
                    }
                    entry<Route.Settings> {
                        SettingsScreen(
                            onRemoveAccount = {
                                backStack.resetTo(Route.AddAccount)
                                unloadAccountModules()
                            },
                            onNavigateBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Route.ArticleList>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { CapyPlaceholder() }
                        )
                    ) {
                        ArticleScreen(
                            onSelectArticle = { id -> backStack.openArticle(id) },
                            onNavigateToSettings = { backStack.add(Route.Settings) },
                            selectedArticleID = (backStack.lastOrNull() as? Route.ArticleDetail)?.articleID,
                            pendingArticleID = pendingArticleID,
                            onPendingArticleSelected = onPendingArticleSelected,
                        )
                    }
                    entry<Route.ArticleDetail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { key ->
                        ArticleDetailScreen(
                            articleID = key.articleID,
                            onBackPressed = { backStack.removeLastOrNull() },
                            onSelectArticle = { id -> backStack.openArticle(id) },
                        )
                    }
                }
            )
        }
    }
}

/**
 * Opens an article in the detail pane. If a detail is already on top (reader next/previous), the
 * top entry is replaced so the back stack stays [list, detail] rather than growing per article.
 */
private fun NavBackStack<NavKey>.openArticle(articleID: String) {
    if (lastOrNull() is Route.ArticleDetail) {
        this[lastIndex] = Route.ArticleDetail(articleID)
    } else {
        add(Route.ArticleDetail(articleID))
    }
}

/** Replaces the entire back stack with [key] (account add/remove transitions). */
private fun NavBackStack<NavKey>.resetTo(key: NavKey) {
    clear()
    add(key)
}

private fun NavBackStack<NavKey>.resetToArticles(appPreferences: AppPreferences) {
    resetTo(Route.ArticleList(appPreferences.filter.get()))
}
