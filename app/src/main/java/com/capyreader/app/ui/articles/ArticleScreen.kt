package com.capyreader.app.ui.articles

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.articles.feeds.FolderActions
import com.capyreader.app.ui.articles.feeds.LocalFolderActions
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.components.SearchState
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
    val articlesSince by viewModel.articlesSince.collectAsStateWithLifecycle()
    val unreadSort by viewModel.unreadSort.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle("")
    val searchState by viewModel.searchState.collectAsStateWithLifecycle(SearchState.INACTIVE)
    val nextFilter by viewModel.nextFilter.collectAsStateWithLifecycle(initialValue = null)
    val afterReadAll by viewModel.afterReadAll.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val refreshInterval by appPreferences
        .refreshInterval
        .collectChangesWithDefault(appPreferences.refreshInterval.get())

    val canSwipeToNextFeed = nextFilter != null

    val fullContent = rememberFullContent(viewModel)
    val articleActions = rememberArticleActions(viewModel)
    val folderActions = rememberFolderActions(viewModel)
    val connectivity = rememberLocalConnectivity()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

//    val pager = remember(filter, unreadSort, articlesSince, searchQuery) {
//        viewModel.pager(
//            filter,
//            unreadSort,
//            articlesSince,
//            searchQuery,
//        )
//    }

    val articles = viewModel.articles.collectAsLazyPagingItems()

    CompositionLocalProvider(
        LocalFullContent provides fullContent,
        LocalArticleActions provides articleActions,
        LocalFolderActions provides folderActions,
        LocalConnectivity provides connectivity,
        LocalArticleLookup provides ArticleLookup(viewModel::findArticlePages),
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
            refreshInterval = refreshInterval,
            onInitialized = { completion ->
                viewModel.initialize(onComplete = completion)
            },
            onRefresh = { filter, completion ->
                viewModel.refresh(filter, completion)
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
            onRemoveFolder = viewModel::removeFolder,
            showUnauthorizedMessage = viewModel.showUnauthorizedMessage,
            onUnauthorizedDismissRequest = viewModel::dismissUnauthorizedMessage,
            onRequestNextFeed = viewModel::requestNextFeed,
            canSwipeToNextFeed = canSwipeToNextFeed,
            openNextFeedOnReadAll = afterReadAll == AfterReadAllBehavior.OPEN_NEXT_FEED,
            search = ArticleSearch(
                query = searchQuery,
                start = { viewModel.startSearch() },
                clear = { viewModel.clearSearch() },
                update = viewModel::updateSearch,
                state = searchState,
            ),
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
fun rememberFolderActions(viewModel: ArticleScreenViewModel): FolderActions {
    return remember {
        FolderActions(
            updateExpanded = viewModel::expandFolder,
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
