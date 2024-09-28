package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.components.ArticleSearch
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
    val statusCount by viewModel.statusCount.collectAsStateWithLifecycle(initialValue = 0)
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsState(initial = null)

    val fullContent = rememberFullContent(viewModel)
    val articleActions = rememberArticleActions(viewModel)

    CompositionLocalProvider(
        LocalFullContent provides fullContent,
        LocalArticleActions provides articleActions
    ) {
        ArticleLayout(
            filter = filter,
            folders = folders,
            feeds = feeds,
            allFolders = allFolders,
            allFeeds = allFeeds,
            articles = viewModel.articles,
            article = viewModel.article,
            statusCount = statusCount,
            refreshInterval = appPreferences.refreshInterval.get(),
            onFeedRefresh = { completion ->
                viewModel.refreshFeed(completion)
            },
            onSelectFolder = viewModel::selectFolder,
            onSelectFeed = viewModel::selectFeed,
            onSelectArticleFilter = viewModel::selectArticleFilter,
            onSelectStatus = viewModel::selectStatus,
            onSelectArticle = viewModel::selectArticle,
            onNavigateToSettings = onNavigateToSettings,
            onRequestClearArticle = viewModel::clearArticle,
            onToggleArticleRead = viewModel::toggleArticleRead,
            onToggleArticleStar = viewModel::toggleArticleStar,
            onMarkAllRead = viewModel::markAllRead,
            onRemoveFeed = viewModel::removeFeed,
            showUnauthorizedMessage = viewModel.showUnauthorizedMessage,
            onUnauthorizedDismissRequest = viewModel::dismissUnauthorizedMessage,
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
