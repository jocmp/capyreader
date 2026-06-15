package com.capyreader.app.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    startBackStack: List<NavKey>,
    appPreferences: AppPreferences,
    deepLink: List<NavKey>? = null,
    onDeepLinkConsumed: () -> Unit = {},
    pendingArticleID: String? = null,
    onPendingArticleSelected: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(*startBackStack.toTypedArray())

    // Warm-start deep links (onNewIntent): replace the back stack with the link's synthetic stack.
    LaunchedEffect(deepLink) {
        val target = deepLink ?: return@LaunchedEffect
        onDeepLinkConsumed()
        backStack.clear()
        backStack.addAll(target)
    }

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
        directive = directive,
        paneExpansionDragHandle = { state ->
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier = Modifier.paneExpansionDraggable(
                    state,
                    LocalMinimumInteractiveComponentSize.current,
                    interactionSource,
                ),
                interactionSource = interactionSource,
            )
        },
    )

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
                            detailPlaceholder = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CapyPlaceholder()
                                }
                            }
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
                        // Stable contentKey: next/previous swaps the article id without remounting
                        // the reader, so the chrome persists and content transitions animate.
                        clazzContentKey = { ARTICLE_DETAIL_CONTENT_KEY },
                        metadata = ListDetailSceneStrategy.detailPane(),
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

private const val ARTICLE_DETAIL_CONTENT_KEY = "article_detail"

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
