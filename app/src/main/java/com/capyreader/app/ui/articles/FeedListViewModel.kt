package com.capyreader.app.ui.articles

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.widget.WidgetUpdater
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.countToday
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class FeedListViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
    private val application: Application,
) : AndroidViewModel(application) {

    private val filter = appPreferences.filter.stateIn(viewModelScope)

    private val _counts = filter.flatMapLatest { latestFilter ->
        account.countAll(latestFilter.status)
    }

    private val _savedSearchCounts = filter.flatMapLatest { latestFilter ->
        account.countAllBySavedSearch(latestFilter.status)
    }

    val topLevelFeeds: Flow<List<Feed>> = combine(
        account.feeds,
        _counts,
        filter,
    ) { feeds, counts, currentFilter ->
        feeds.filter { !it.isPages }
            .map { copyFeedCounts(it, counts) }
            .withPositiveCount(currentFilter.status)
    }

    val pagesFeed: Flow<Feed?> = combine(
        account.feeds,
        _counts,
        filter,
    ) { feeds, counts, currentFilter ->
        feeds.find { it.isPages }
            ?.let { copyFeedCounts(it, counts) }
            ?.takeIf { it.count > 0 || currentFilter.status != ArticleStatus.UNREAD }
    }

    val allFeeds: Flow<List<Feed>> = account.taggedFeeds

    val allFolders: Flow<List<Folder>> = account.folders

    val folders: Flow<List<Folder>> = combine(
        account.folders,
        _counts,
        filter,
    ) { folders, counts, currentFilter ->
        folders.map { copyFolderCounts(it, counts, currentFilter) }
            .withPositiveCount(currentFilter.status)
    }

    val savedSearches: Flow<List<SavedSearch>> = combine(
        account.savedSearches,
        _savedSearchCounts,
        filter,
    ) { searches, counts, currentFilter ->
        searches.map { copySavedSearchCounts(it, counts) }
            .withPositiveCount(currentFilter.status)
    }

    val allSavedSearches: Flow<List<SavedSearch>> = account.savedSearches

    val statusCount: Flow<Long> = filter.flatMapLatest { latestFilter ->
        account.countAllByStatus(countableStatus(latestFilter))
    }

    val todayCount: Flow<Long> = _counts.combine(filter) { _, currentFilter ->
        account.countToday(countableStatus(currentFilter))
    }

    val showTodayFilter: Flow<Boolean> = appPreferences.showTodayFilter.stateIn(viewModelScope)

    val showOnboarding: Flow<Boolean> = allFeeds.map { it.isEmpty() }

    val source = account.source

    fun removeFeed(feedID: String, onSuccess: () -> Unit) {
        viewModelScope.launchIO {
            account.removeFeed(feedID = feedID).onSuccess { onSuccess() }
        }
    }

    fun removeFolder(folderTitle: String, completion: (Result<Unit>) -> Unit) {
        viewModelScope.launchIO {
            account.removeFolder(folderTitle).fold(
                onSuccess = { completion(Result.success(Unit)) },
                onFailure = { completion(Result.failure(it)) }
            )
        }
    }

    fun expandFolder(folderName: String, expanded: Boolean) {
        viewModelScope.launchIO {
            account.expandFolder(folderName, expanded = expanded)
        }
    }

    fun reloadFavicon(feedID: String) {
        viewModelScope.launchIO {
            account.reloadFavicon(feedID)
        }
    }

    fun updateOpenInBrowser(feedID: String, enabled: Boolean) {
        viewModelScope.launchIO {
            account.updateOpenInBrowser(feedID, enabled = enabled)
            WidgetUpdater.update(context = context)
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

    private fun copyFolderCounts(
        folder: Folder,
        counts: Map<String, Long>,
        filter: ArticleFilter,
    ): Folder {
        val folderFeeds = folder.feeds.map { copyFeedCounts(it, counts) }
        return folder.copy(
            feeds = folderFeeds.withPositiveCount(filter.status).toMutableList(),
            count = folderFeeds.sumOf { it.count }
        )
    }

    private fun copyFeedCounts(feed: Feed, counts: Map<String, Long>): Feed =
        feed.copy(count = counts.getOrDefault(feed.id, 0))

    private fun copySavedSearchCounts(savedSearch: SavedSearch, counts: Map<String, Long>): SavedSearch =
        savedSearch.copy(count = counts.getOrDefault(savedSearch.id, 0))

    private val context: Context
        get() = application.applicationContext
}
