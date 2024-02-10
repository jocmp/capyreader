package com.jocmp.basilreader.ui.articles

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.androidx.compose.koinViewModel

private const val TAG = "ArticleScreen"

@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onAddFeed: () -> Unit,
    onEditFeed: (feedID: String) -> Unit,
    onEditFolder: (folderTitle: String) -> Unit,
    onNavigateToAccounts: () -> Unit,
) {
    ArticleLayout(
        filter = viewModel.filter,
        folders = viewModel.folders,
        feeds = viewModel.feeds,
        article = viewModel.article,
        statusCount = viewModel.statusCount,
        onFeedRefresh = { completion ->
            viewModel.refreshFeed(completion)
        },
        onAddFeed = onAddFeed,
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

    LaunchedEffect(Unit) {
        Log.d(TAG, "ArticleScreen: refreshed")
        viewModel.reload()
    }
}
