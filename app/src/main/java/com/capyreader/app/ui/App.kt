package com.capyreader.app.ui

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.capyreader.app.ui.articles.LocalArticlePaneExpansion
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.articles.media.MediaSceneStrategy
import com.capyreader.app.ui.articles.media.MediaScreen
import com.capyreader.app.ui.articles.rememberArticlePaneExpansion
import com.capyreader.app.ui.shared.materialSharedAxisXIn
import com.capyreader.app.ui.shared.materialSharedAxisXOut
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
    val mediaSceneStrategy = remember { MediaSceneStrategy() }
    val paneExpansion = rememberArticlePaneExpansion()
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
        directive = directive,
        paneExpansionState = paneExpansion.state,
        paneExpansionDragHandle = { state ->
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier = Modifier
                    .padding(horizontal = HandleEdgeInset)
                    .paneExpansionDraggable(
                        state,
                        LocalMinimumInteractiveComponentSize.current,
                        interactionSource,
                    ),
                interactionSource = interactionSource,
            )
        },
    )

    // Window-level drawer: hosting it above NavDisplay lets its scrim cover both panes (the list
    // entry publishes the content + drives open/close through LocalAppDrawer).
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var drawerContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    val drawerController = remember(drawerState) {
        AppDrawerController(state = drawerState, setContent = { drawerContent = it })
    }

    CapyTheme(appPreferences) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
          ModalNavigationDrawer(
              drawerState = drawerState,
              gesturesEnabled = drawerState.isOpen,
              drawerContent = {
                  ModalDrawerSheet { drawerContent?.invoke() }
              },
          ) {
          CompositionLocalProvider(
              LocalArticlePaneExpansion provides paneExpansion,
              LocalAppDrawer provides drawerController,
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
                sceneStrategies = listOf(mediaSceneStrategy, listDetailStrategy),
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
                            onSelectArticle = { id, searchQuery ->
                                backStack.openArticle(id, searchQuery)
                            },
                            onNavigateToSettings = { backStack.add(Route.Settings) },
                            selectedArticleID = (backStack.lastOrNull() as? Route.ArticleDetail)?.articleID,
                        )
                    }
                    entry<Route.ArticleDetail>(
                        // Stable contentKey: next/previous swaps the article id without remounting
                        // the reader, so the chrome persists and content transitions animate.
                        clazzContentKey = { ARTICLE_DETAIL_CONTENT_KEY },
                        // On phone (single pane) the list <-> detail navigation is a scene change;
                        // animate it with the shared-axis-X motion the pane scaffold used originally
                        // instead of the default cross-fade. On tablet both panes share one scene,
                        // so this never fires there.
                        metadata = ListDetailSceneStrategy.detailPane() +
                            NavDisplay.transitionSpec {
                                sharedAxisXEnter(forward = true) togetherWith
                                    sharedAxisXExit(forward = true)
                            } +
                            NavDisplay.popTransitionSpec {
                                sharedAxisXEnter(forward = false) togetherWith
                                    sharedAxisXExit(forward = false)
                            } +
                            NavDisplay.predictivePopTransitionSpec {
                                sharedAxisXEnter(forward = false) togetherWith
                                    sharedAxisXExit(forward = false)
                            },
                    ) { key ->
                        ArticleDetailScreen(
                            articleID = key.articleID,
                            searchQuery = key.searchQuery,
                            onBackPressed = { backStack.removeLastOrNull() },
                            onSelectArticle = { id -> backStack.openArticle(id, key.searchQuery) },
                            onSelectMedia = { media -> backStack.add(Route.MediaViewer(media)) },
                        )
                    }
                    entry<Route.MediaViewer>(
                        metadata = MediaSceneStrategy.overlay(),
                    ) { key ->
                        MediaScreen(
                            media = key.media,
                            onDismiss = { backStack.removeLastOrNull() },
                        )
                    }
                }
            )
          }
          }
        }
    }
}

private const val ARTICLE_DETAIL_CONTENT_KEY = "article_detail"

/** Matches the pane scaffold's original shared-axis offset (10% of the pane width). */
private const val PANE_OFFSET_FACTOR = 0.10f

private fun sharedAxisXEnter(forward: Boolean) =
    materialSharedAxisXIn(initialOffsetX = { width ->
        val offset = (width * PANE_OFFSET_FACTOR).toInt()
        if (forward) offset else -offset
    })

private fun sharedAxisXExit(forward: Boolean) =
    materialSharedAxisXOut(targetOffsetX = { width ->
        val offset = (width * PANE_OFFSET_FACTOR).toInt()
        if (forward) -offset else offset
    })

private val HandleEdgeInset = 16.dp

/**
 * Opens an article in the detail pane. If a detail is already on top (reader next/previous), the
 * top entry is replaced so the back stack stays [list, detail] rather than growing per article.
 */
private fun NavBackStack<NavKey>.openArticle(articleID: String, searchQuery: String? = null) {
    if (lastOrNull() is Route.ArticleDetail) {
        this[lastIndex] = Route.ArticleDetail(articleID, searchQuery)
    } else {
        add(Route.ArticleDetail(articleID, searchQuery))
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
