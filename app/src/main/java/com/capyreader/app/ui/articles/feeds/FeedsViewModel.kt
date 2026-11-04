package com.capyreader.app.ui.articles.feeds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleListVerticalSwipe
import com.capyreader.app.ui.articles.ArticleSessionCutoff
import com.capyreader.app.ui.articles.countableStatus
import com.capyreader.app.ui.articles.withPositiveCount
import com.capyreader.app.ui.widget.WidgetUpdater
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.ArticleStatus.UNREAD
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.articles.SidebarItem
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.countToday
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * The feed list: feeds, folders, and saved searches with their counts, and selecting among them.
 *
 * Selection is shared with the article list through [AppPreferences.filter]: this view model
 * writes it, and the list follows it. Status changes made by the list flow back the same way.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedsViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
    private val application: Application,
    private val notificationHelper: NotificationHelper,
    private val articleCutoff: ArticleSessionCutoff,
) : AndroidViewModel(application) {
    private val _filter = MutableStateFlow(appPreferences.filter.get())

    val filter: StateFlow<ArticleFilter> = _filter

    val source = account.source

    private val listSwipeBottom =
        appPreferences.articleListOptions.swipeBottom.stateIn(viewModelScope)

    private val counts = filter.flatMapLatest { latestFilter ->
        account.countAll(latestFilter.status)
    }

    private val savedSearchCounts = filter.flatMapLatest { latestFilter ->
        account.countAllBySavedSearch(latestFilter.status)
    }

    val folders: Flow<List<Folder>> = combine(
        account.folders,
        counts,
        filter,
    ) { folders, latestCounts, filter ->
        folders.map { copyFolderCounts(it, latestCounts, filter) }
            .withPositiveCount(filter.status)
    }

    val savedSearches: Flow<List<SavedSearch>> = combine(
        account.savedSearches,
        savedSearchCounts,
        filter,
    ) { searches, latestCounts, filter ->
        searches.map { copySavedSearchCounts(it, latestCounts) }
            .withPositiveCount(filter.status)
    }

    val topLevelFeeds: Flow<List<Feed>> = combine(
        account.feeds,
        counts,
        filter,
    ) { feeds, latestCounts, filter ->
        feeds.filter { !it.isReadLater }
            .map { copyFeedCounts(it, latestCounts) }
            .withPositiveCount(filter.status)
    }

    val readLaterFeed: Flow<Feed?> = combine(
        account.feeds,
        counts,
    ) { feeds, latestCounts ->
        feeds.find { it.isReadLater }
            ?.let { copyFeedCounts(it, latestCounts) }
    }

    val statusCount: Flow<Long> = filter.flatMapLatest { latestFilter ->
        account.countAllByStatus(countableStatus(latestFilter))
    }

    val todayCount: Flow<Long> = counts.combine(filter) { _, filter ->
        account.countToday(countableStatus(filter))
    }

    private val sidebar: Flow<List<SidebarItem>> = combine(
        readLaterFeed,
        savedSearches,
        folders,
        topLevelFeeds,
        filter,
    ) { readLater, searches, folders, feeds, filter ->
        SidebarItem.buildList(
            status = filter.status,
            readLaterFeed = readLater,
            savedSearches = searches,
            folders = folders,
            feeds = feeds,
        )
    }

    private val _sidebarItem = MutableStateFlow<SidebarItem?>(null)

    private val sidebarListener: Flow<SidebarItem?> =
        combine(
            listSwipeBottom,
            sidebar,
            filter,
        ) { swipeBottom, sidebar, filter ->
            if (swipeBottom == ArticleListVerticalSwipe.DISABLED) {
                return@combine null
            }

            val found = sidebar.find { it.isSelected(filter) }
            if (found != null) return@combine found

            val stale = _sidebarItem.value?.takeIf { it.isSelected(filter) }
                ?: return@combine null

            SidebarItem(
                toFilter = stale.toFilter,
                isSelected = stale.isSelected,
                next = findFreshItem(stale, sidebar, filter.status),
            )
        }

    /** The feed or folder after the current selection, for swipe-to-next and read-all. */
    val nextFilter: Flow<SidebarItem?> = _sidebarItem.map { it?.next }

    init {
        viewModelScope.launch {
            appPreferences.filter.changes().collect { persisted ->
                if (persisted != latestFilter) {
                    _filter.value = persisted
                }
            }
        }

        viewModelScope.launch {
            sidebarListener.collect {
                _sidebarItem.value = it
            }
        }
    }

    fun selectArticleFilter() {
        updateFilter(ArticleFilter.Articles(articleStatus = currentStatus))
    }

    fun selectToday() {
        updateFilter(ArticleFilter.Today(todayStatus = currentStatus))
    }

    fun selectFeed(feedID: String, folderTitle: String? = null) {
        viewModelScope.launchIO {
            val feed = account.findFeed(feedID) ?: return@launchIO
            updateFilter(
                ArticleFilter.Feeds(
                    feedID = feed.id,
                    folderTitle = folderTitle,
                    feedStatus = currentStatus
                )
            )
        }
    }

    fun selectSavedSearch(savedSearchID: String) {
        viewModelScope.launchIO {
            val savedSearch = account.findSavedSearch(savedSearchID) ?: return@launchIO
            updateFilter(
                ArticleFilter.SavedSearches(savedSearch.id, savedSearchStatus = currentStatus)
            )
        }
    }

    fun selectFolder(title: String) {
        viewModelScope.launchIO {
            val folder = account.findFolder(title) ?: return@launchIO
            updateFilter(
                ArticleFilter.Folders(folderTitle = folder.title, folderStatus = currentStatus)
            )
        }
    }

    fun requestNextFeed() {
        _sidebarItem.value?.next?.let(::selectSidebarItem)
    }

    /**
     * After the list marks everything read: move to the next feed or folder with unread articles,
     * or call [onArticlesCleared] when there's none left.
     */
    fun openNextFeedOnAllRead(onArticlesCleared: () -> Unit) {
        viewModelScope.launchIO {
            val current = _sidebarItem.value
            val next = sidebar.first()

            val nextItem = current?.let { stale ->
                val matched = next.find { it.isSelected(stale.toFilter(latestFilter.status)) }
                matched?.next ?: findFreshItem(stale, next, latestFilter.status)
            }

            if (nextItem != null) {
                selectSidebarItem(nextItem)
            } else {
                if (latestFilter.status == UNREAD) {
                    selectArticleFilter()
                }
                onArticlesCleared()
            }
        }
    }

    /** Marks a feed, folder, or saved search read from its long-press menu. */
    fun markAllRead(filter: ArticleFilter) {
        viewModelScope.launchIO {
            val articleIDs = account.unreadArticleIDs(
                filter = filter,
                range = MarkRead.All,
                sortOrder = appPreferences.articleListOptions.getSortOrder(filter).get(),
                query = null,
                since = articleCutoff.value,
            )

            account.markAllRead(articleIDs)

            launchIO {
                notificationHelper.dismissNotifications(articleIDs)
            }
        }
    }

    fun removeFeed(
        feedID: String,
        completion: (result: Result<Unit>) -> Unit,
    ) {
        viewModelScope.launchIO {
            account.removeFeed(feedID = feedID).fold(
                onSuccess = {
                    resetToDefaultFilter()
                    completion(Result.success(Unit))
                },
                onFailure = {
                    completion(Result.failure(it))
                }
            )
        }
    }

    fun removeFolder(
        folderTitle: String,
        completion: (result: Result<Unit>) -> Unit
    ) {
        viewModelScope.launchIO {
            account.removeFolder(folderTitle).fold(
                onSuccess = {
                    resetToDefaultFilter()
                    completion(Result.success(Unit))
                },
                onFailure = {
                    completion(Result.failure(it))
                }
            )
        }
    }

    fun expandFolder(folderName: String, expanded: Boolean) {
        viewModelScope.launchIO {
            account.expandFolder(folderName, expanded = expanded)
        }
    }

    fun updateOpenInBrowser(feedID: String, enabled: Boolean) {
        viewModelScope.launchIO {
            account.updateOpenInBrowser(feedID, enabled = enabled)
            WidgetUpdater.update(context = application.applicationContext)
        }
    }

    fun toggleFeedUnreadBadge(feedID: String, enabled: Boolean) {
        viewModelScope.launchIO {
            account.toggleFeedUnreadBadge(feedID, enabled)
        }
    }

    fun toggleSavedSearchUnreadBadge(id: String, enabled: Boolean) {
        viewModelScope.launchIO {
            account.toggleSavedSearchUnreadBadge(id, enabled)
        }
    }

    fun reloadFavicon(feedID: String) {
        viewModelScope.launchIO {
            account.reloadFavicon(feedID)
        }
    }

    private val latestFilter: ArticleFilter
        get() = filter.value

    private val currentStatus: ArticleStatus
        get() = latestFilter.status

    private fun selectSidebarItem(item: SidebarItem) {
        updateFilter(item.toFilter(currentStatus))
    }

    private fun resetToDefaultFilter() {
        updateFilter(ArticleFilter.default())
    }

    private fun updateFilter(filter: ArticleFilter) {
        _filter.value = filter

        appPreferences.filter.set(filter)
    }

    private fun findFreshItem(
        stale: SidebarItem,
        sidebar: List<SidebarItem>,
        status: ArticleStatus,
    ): SidebarItem? {
        var search: SidebarItem? = stale.next

        while (search != null) {
            val searchFilter = search.toFilter(status)
            val match = sidebar.find { it.isSelected(searchFilter) }
            if (match != null) {
                return match
            }
            search = search.next
        }

        return null
    }

    private fun copyFolderCounts(
        folder: Folder,
        counts: Map<String, Long>,
        filter: ArticleFilter
    ): Folder {
        val folderFeeds = folder.feeds.map { copyFeedCounts(it, counts) }

        return folder.copy(
            feeds = folderFeeds.withPositiveCount(filter.status).toMutableList(),
            count = folderFeeds.sumOf { it.count }
        )
    }

    private fun copyFeedCounts(feed: Feed, counts: Map<String, Long>): Feed {
        return feed.copy(count = counts.getOrDefault(feed.id, 0))
    }

    private fun copySavedSearchCounts(
        savedSearch: SavedSearch,
        counts: Map<String, Long>
    ): SavedSearch {
        return savedSearch.copy(count = counts.getOrDefault(savedSearch.id, 0))
    }
}
