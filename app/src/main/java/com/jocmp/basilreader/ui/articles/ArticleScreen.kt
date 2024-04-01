package com.jocmp.basilreader.ui.articles

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

private const val TAG = "ArticleScreen"

@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onEditFeed: (feedID: String) -> Unit,
    onEditFolder: (folderTitle: String) -> Unit,
    onNavigateToAccounts: () -> Unit,
) {
    val feeds by viewModel.feeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val statusCount by viewModel.statusCount.collectAsStateWithLifecycle(initialValue = 0)
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    ArticleLayout(
        filter = filter,
        folders = folders,
        feeds = feeds,
        article = viewModel.article,
        statusCount = statusCount,
        onFeedRefresh = { completion ->
            viewModel.refreshFeed(completion)
        },
        onClearArticle = viewModel::clearArticle,
        onEditFeed = onEditFeed,
        onEditFolder = onEditFolder,
        onNavigateToAccounts = onNavigateToAccounts,
        onSelectArticleFilter = viewModel::selectArticleFilter,
        onSelectFeed = viewModel::selectFeed,
        onSelectFolder = viewModel::selectFolder,
        onSelectStatus = viewModel::selectStatus,
        onRemoveFeed = viewModel::removeFeed,
        onRemoveFolder = viewModel::removeFolder,
        articles = viewModel.articles,
        onSelectArticle = viewModel::selectArticle,
        onToggleArticleRead = viewModel::toggleArticleRead,
        onToggleArticleStar = viewModel::toggleArticleStar,
    )
}
