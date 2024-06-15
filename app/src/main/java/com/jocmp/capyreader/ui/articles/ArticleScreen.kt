package com.jocmp.capyreader.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleScreen(
    viewModel: ArticleScreenViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit,
) {
    val feeds by viewModel.feeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFeeds by viewModel.allFeeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val statusCount by viewModel.statusCount.collectAsStateWithLifecycle(initialValue = 0)
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    ArticleLayout(
        filter = filter,
        folders = folders,
        feeds = feeds,
        allFeeds = allFeeds,
        articles = viewModel.articles,
        article = viewModel.article,
        statusCount = statusCount,
        onFeedRefresh = { completion ->
            viewModel.refreshFeed(completion)
        },
        onSelectFolder = viewModel::selectFolder,
        onSelectFeed = viewModel::selectFeed,
        onSelectArticleFilter = viewModel::selectArticleFilter,
        onSelectStatus = viewModel::selectStatus,
        onSelectArticle = viewModel::selectArticle,
        onRemoveFeed = viewModel::removeFeed,
        onNavigateToSettings = onNavigateToSettings,
        onClearArticle = viewModel::clearArticle,
        onToggleArticleRead = viewModel::toggleArticleRead,
        onToggleArticleStar = viewModel::toggleArticleStar,
        onMarkAllRead = viewModel::markAllRead,
    )
}
