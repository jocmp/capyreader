package com.capyreader.app.ui.articles

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.common.AfterReadAllBehavior
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.rememberLocalConnectivity
import com.jocmp.capy.common.launchUI
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ArticleScreen(
    viewModel: ArticleScreenViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject(),
    onNavigateToSettings: () -> Unit,
) {
    val feeds by viewModel.feeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFeeds by viewModel.allFeeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val savedSearches by viewModel.savedSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    val statusCount by viewModel.statusCount.collectAsStateWithLifecycle(initialValue = 0)
    val filter by viewModel.filter.collectAsStateWithLifecycle(appPreferences.filter.get())
    val searchQuery by viewModel.searchQuery.collectAsState(initial = null)
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val nextFilter by viewModel.nextFilter.collectAsStateWithLifecycle(initialValue = null)
    val canSwipeToNextFeed = nextFilter != null
    val afterReadAll by viewModel.afterReadAll.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val fullContent = rememberFullContent(viewModel)
    val articleActions = rememberArticleActions(viewModel)
    val connectivity = rememberLocalConnectivity()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    CompositionLocalProvider(
        LocalFullContent provides fullContent,
        LocalArticleActions provides articleActions,
        LocalConnectivity provides connectivity
    ) {
        ArticleLayout(
            filter = filter,
            folders = folders,
            savedSearches = savedSearches,
            feeds = feeds,
            allFolders = allFolders,
            allFeeds = allFeeds,
            articles = articles,
            article = viewModel.article,
            statusCount = statusCount,
            refreshInterval = appPreferences.refreshInterval.get(),
            onFeedRefresh = { completion ->
                viewModel.refreshFeed(completion)
            },
            drawerState = drawerState,
            onSelectFolder = viewModel::selectFolder,
            onSelectFeed = viewModel::selectFeed,
            onSelectSavedSearch = viewModel::selectSavedSearch,
            onSelectArticleFilter = viewModel::selectArticleFilter,
            onSelectStatus = viewModel::selectStatus,
            onSelectArticle = viewModel::selectArticle,
            onNavigateToSettings = onNavigateToSettings,
            onRequestClearArticle = viewModel::clearArticle,
            onToggleArticleRead = viewModel::toggleArticleRead,
            onToggleArticleStar = viewModel::toggleArticleStar,
            onMarkAllRead = { range ->
                viewModel.markAllRead(
                    onArticlesCleared = {
                        scope.launchUI {
                            drawerState.open()
                        }
                    },
                    searches = savedSearches,
                    folders = folders,
                    feeds = feeds,
                    range = range,
                )
            },
            onRemoveFeed = viewModel::removeFeed,
            showUnauthorizedMessage = viewModel.showUnauthorizedMessage,
            onUnauthorizedDismissRequest = viewModel::dismissUnauthorizedMessage,
            onRequestNextFeed = viewModel::requestNextFeed,
            canSwipeToNextFeed = canSwipeToNextFeed,
            openNextFeedOnReadAll = afterReadAll == AfterReadAllBehavior.OPEN_NEXT_FEED,
            search = ArticleSearch(
                query = searchQuery,
                clear = { viewModel.clearSearch() },
                update = viewModel::updateSearch,
            )
        )
    }
}

@Composable
fun rememberArticleActions(viewModel: ArticleScreenViewModel): ArticleActions {
    return remember {
        ArticleActions(
            markRead = viewModel::markReadAsync,
            markUnread = viewModel::markUnreadAsync,
            star = viewModel::addStarAsync,
            unstar = viewModel::removeStarAsync,
        )
    }
}

@Composable
fun rememberFullContent(viewModel: ArticleScreenViewModel): FullContentFetcher {
    return remember {
        FullContentFetcher(
            fetch = viewModel::fetchFullContentAsync,
            reset = viewModel::resetFullContent,
        )
    }
}
