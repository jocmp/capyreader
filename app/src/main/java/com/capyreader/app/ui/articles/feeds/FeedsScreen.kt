package com.capyreader.app.ui.articles.feeds

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.LocalAppDrawer
import com.capyreader.app.ui.LocalBadgeStyle
import com.capyreader.app.ui.articles.ArticleScreenViewModel
import com.capyreader.app.ui.articles.rememberFeedActions
import com.capyreader.app.ui.articles.rememberFolderActions
import com.capyreader.app.ui.articles.rememberSavedSearchActions
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.common.launchUI
import org.koin.compose.koinInject

/**
 * The [com.capyreader.app.ui.Route.Feeds] entry: a full page on compact windows, the drawer's
 * content on medium and wider. Selecting a list applies the filter, then [onOpenList] shows it.
 */
@Composable
fun FeedsScreen(
    viewModel: ArticleScreenViewModel,
    onOpenList: () -> Unit,
    onCloseArticle: () -> Unit,
    onNavigateToSettings: () -> Unit,
    appPreferences: AppPreferences = koinInject(),
) {
    val feeds by viewModel.topLevelFeeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val readLaterFeed by viewModel.readLaterFeed.collectAsStateWithLifecycle(initialValue = null)
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val savedSearches by viewModel.savedSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    val statusCount by viewModel.statusCount.collectAsStateWithLifecycle(initialValue = 0)
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle(initialValue = 0)
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val refreshAllState by viewModel.refreshAllState.collectAsStateWithLifecycle(
        initialValue = AngleRefreshState.STOPPED
    )
    val badgeStyle by appPreferences.badgeStyle.collectChangesWithDefault()
    val drawer = LocalAppDrawer.current
    val scope = rememberCoroutineScope()

    fun select(isSelected: Boolean, action: () -> Unit) {
        if (!isSelected) {
            onCloseArticle()
            action()
        }
        scope.launchUI { drawer?.close() }
        onOpenList()
    }

    CompositionLocalProvider(
        LocalBadgeStyle provides badgeStyle,
        LocalFeedActions provides rememberFeedActions(viewModel),
        LocalFolderActions provides rememberFolderActions(viewModel),
        LocalSavedSearchActions provides rememberSavedSearchActions(viewModel),
    ) {
        Surface(
            color = DrawerDefaults.modalContainerColor,
            modifier = Modifier.fillMaxSize(),
        ) {
            FeedList(
                source = viewModel.source,
                filter = filter,
                statusCount = statusCount,
                todayCount = todayCount,
                folders = folders,
                feeds = feeds,
                readLaterFeed = readLaterFeed,
                savedSearches = savedSearches,
                onFilterSelect = {
                    select(filter.hasArticlesSelected()) { viewModel.selectArticleFilter() }
                },
                onSelectToday = {
                    select(filter.hasTodaySelected()) { viewModel.selectToday() }
                },
                onSelectSavedSearch = { savedSearch ->
                    select(filter.isSavedSearchSelected(savedSearch)) {
                        viewModel.selectSavedSearch(savedSearch.id)
                    }
                },
                onSelectFolder = { folder ->
                    select(filter.isFolderSelected(folder)) { viewModel.selectFolder(folder.title) }
                },
                onSelectFeed = { feed, folderTitle ->
                    select(filter.isFeedSelected(feed)) { viewModel.selectFeed(feed.id, folderTitle) }
                },
                refreshState = refreshAllState,
                onRefresh = { viewModel.refreshAll() },
                onMarkAllRead = { viewModel.markAllRead(filter = it) },
                onFeedAdded = { feedID -> select(isSelected = false) { viewModel.selectFeed(feedID) } },
                onNavigateToSettings = {
                    scope.launchUI { drawer?.close() }
                    onNavigateToSettings()
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            )
        }
    }
}
