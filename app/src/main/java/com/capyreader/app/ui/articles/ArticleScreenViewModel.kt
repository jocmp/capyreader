package com.capyreader.app.ui.articles

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.capyreader.app.R
import com.capyreader.app.common.toast
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleListVerticalSwipe
import com.capyreader.app.sync.Sync
import com.capyreader.app.ui.components.SearchState
import com.capyreader.app.ui.widget.WidgetUpdater
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.ArticleStatus.STARRED
import com.jocmp.capy.ArticleStatus.UNREAD
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.articles.NextFilter
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.withUIContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleScreenViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
    private val application: Application,
    private val notificationHelper: NotificationHelper,
) : AndroidViewModel(application) {

    private var refreshJob: Job? = null

    val filter = appPreferences.filter.stateIn(viewModelScope)

    private val listSwipeBottom =
        appPreferences.articleListOptions.swipeBottom.stateIn(viewModelScope)

    private val _searchQuery = MutableStateFlow("")
    private val _searchState = MutableStateFlow(SearchState.INACTIVE)
    private val _nextFilter = MutableStateFlow<NextFilter?>(null)

    var refreshingAll by mutableStateOf(false)
        private set

    val articlesSince = MutableStateFlow<OffsetDateTime>(OffsetDateTime.now())

    private var _showUnauthorizedMessage by mutableStateOf(UnauthorizedMessageState.HIDE)

    val sortOrder = appPreferences.articleListOptions.sortOrder.stateIn(viewModelScope)
    val afterReadAll = appPreferences.articleListOptions.afterReadAllBehavior.stateIn(viewModelScope)

    val articles: Flow<PagingData<Article>> =
        combine(filter, _searchQuery, articlesSince, sortOrder) { filter, query, since, sort ->
            account.buildArticlePager(filter = filter, query = query, sortOrder = sort, since = since).flow
        }.flatMapLatest { it }

    val unreadCount: Flow<Long> = combine(
        filter,
        _searchQuery,
    ) { filter, query ->
        account.countUnread(filter = filter, query = query)
    }.flatMapLatest { it }

    val searchQuery: Flow<String> get() = _searchQuery
    val searchState: Flow<SearchState> get() = _searchState
    val nextFilter: Flow<NextFilter?> get() = _nextFilter

    val showUnauthorizedMessage: Boolean
        get() = _showUnauthorizedMessage == UnauthorizedMessageState.SHOW

    val source = account.source

    val canSaveArticleExternally = account.canSaveArticleExternally.stateIn(viewModelScope)

    private val nextFilterListener: Flow<NextFilter?> =
        combine(
            listSwipeBottom,
            account.savedSearches,
            account.feeds,
            account.folders,
            filter
        ) { swipeBottom, savedSearches, feeds, folders, filter ->
            if (swipeBottom == ArticleListVerticalSwipe.DISABLED) return@combine null
            NextFilter.findSwipeDestination(
                filter,
                searches = savedSearches,
                folders = folders,
                feeds = feeds,
            )
        }

    init {
        viewModelScope.launch {
            nextFilterListener.collect { _nextFilter.value = it }
        }
    }

    // region Filter selection

    fun selectArticleFilter() {
        updateFilter(ArticleFilter.default().withStatus(status = latestFilter.status))
    }

    fun selectStatus(status: ArticleStatus) {
        updateFilter(latestFilter.withStatus(status = status))
    }

    fun selectToday() {
        updateFilter(ArticleFilter.Today(todayStatus = latestFilter.status))
    }

    fun selectFeed(feedID: String, folderTitle: String? = null) {
        viewModelScope.launchIO {
            val feed = account.findFeed(feedID) ?: return@launchIO
            updateFilter(
                ArticleFilter.Feeds(
                    feedID = feed.id,
                    folderTitle = folderTitle,
                    feedStatus = latestFilter.status
                )
            )
        }
    }

    fun selectSavedSearch(savedSearchID: String) {
        viewModelScope.launchIO {
            val savedSearch = account.findSavedSearch(savedSearchID) ?: return@launchIO
            updateFilter(
                ArticleFilter.SavedSearches(savedSearch.id, savedSearchStatus = latestFilter.status)
            )
        }
    }

    fun selectFolder(title: String) {
        viewModelScope.launchIO {
            val folder = account.findFolder(title) ?: return@launchIO
            updateFilter(
                ArticleFilter.Folders(folderTitle = folder.title, folderStatus = latestFilter.status)
            )
        }
    }

    fun requestNextFeed() {
        _nextFilter.value?.let(::selectNextFilter)
    }

    internal fun resetToDefaultFilter() {
        updateFilter(ArticleFilter.default().copy(latestFilter.status))
    }

    // endregion

    // region Article list actions

    fun markAllRead(
        onArticlesCleared: () -> Unit,
        range: MarkRead,
        searches: List<SavedSearch>,
        folders: List<Folder>,
        feeds: List<Feed>,
    ) {
        viewModelScope.launchIO {
            val articleIDs = account.unreadArticleIDs(
                filter = latestFilter,
                range = range,
                sortOrder = sortOrder.value,
                query = _searchQuery.value,
            )

            account.markAllRead(articleIDs).onFailure {
                Sync.markReadAsync(articleIDs, context)
            }

            launchIO { notificationHelper.dismissNotifications(articleIDs) }

            if (range != MarkRead.All) return@launchIO

            when (afterReadAll.value) {
                AfterReadAllBehavior.OPEN_DRAWER -> onArticlesCleared()
                AfterReadAllBehavior.OPEN_NEXT_FEED ->
                    openNextFeedOnAllRead(onArticlesCleared, searches, folders, feeds)
                else -> Unit
            }
        }
    }

    fun markReadAsync(articleID: String) = viewModelScope.launchIO { markRead(articleID) }

    fun markUnreadAsync(articleID: String) = viewModelScope.launchIO { markUnread(articleID) }

    fun addStarAsync(articleID: String) = viewModelScope.launchIO { addStar(articleID) }

    fun removeStarAsync(articleID: String) = viewModelScope.launchIO { removeStar(articleID) }

    // endregion

    // region Search

    fun startSearch() { _searchState.value = SearchState.ACTIVE }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchState.value = SearchState.INACTIVE
    }

    fun updateSearch(query: String) { _searchQuery.value = query }

    // endregion

    // region Refresh

    fun refresh(filter: ArticleFilter, onComplete: () -> Unit) {
        updateArticlesSince()
        refreshFilter(filter) {
            updateArticlesSince()
            onComplete()
        }
    }

    fun refreshAll(onComplete: () -> Unit) {
        refreshingAll = true
        refresh(ArticleFilter.default()) {
            onComplete()
            refreshJob?.invokeOnCompletion { refreshingAll = false }
        }
    }

    // endregion

    // region Labels & external save

    fun getArticleLabels(articleID: String?): Flow<List<String>> {
        articleID ?: return emptyFlow()
        return account.getArticleSavedSearches(articleID)
    }

    fun addLabelAsync(articleID: String, savedSearchID: String) {
        viewModelScope.launchIO { account.addSavedSearch(articleID, savedSearchID) }
    }

    fun removeLabelAsync(articleID: String, savedSearchID: String) {
        viewModelScope.launchIO { account.removeSavedSearch(articleID, savedSearchID) }
    }

    suspend fun createLabel(articleID: String, name: String): Result<String> {
        return account.createSavedSearch(name).fold(
            onSuccess = { labelID ->
                account.addSavedSearch(articleID, labelID).fold(
                    onSuccess = { Result.success(labelID) },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { Result.failure(it) }
        )
    }

    fun saveArticleExternallyAsync(articleID: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launchIO {
            val result = account.saveArticleExternally(articleID)
            withUIContext { onComplete(result) }
        }
    }

    // endregion

    // region Auth

    fun dismissUnauthorizedMessage() {
        _showUnauthorizedMessage = UnauthorizedMessageState.LATER
    }

    // endregion

    private fun updateFilter(filter: ArticleFilter) {
        appPreferences.filter.set(filter)
        updateArticlesSince()
    }

    private fun updateArticlesSince() {
        articlesSince.value = OffsetDateTime.now().plusSeconds(1)
    }

    private fun refreshFilter(filter: ArticleFilter, onComplete: () -> Unit) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launchIO {
            account.refresh(filter).onFailure { throwable ->
                if (throwable is UnauthorizedError && _showUnauthorizedMessage == UnauthorizedMessageState.HIDE) {
                    _showUnauthorizedMessage = UnauthorizedMessageState.SHOW
                }
            }
            launchIO { WidgetUpdater.update(context) }
            onComplete()
        }
    }

    private fun openNextFeedOnAllRead(
        onArticlesCleared: () -> Unit,
        searches: List<SavedSearch>,
        folders: List<Folder>,
        feeds: List<Feed>,
    ) {
        val nextFilter = NextFilter.findMarkReadDestination(latestFilter, searches, folders, feeds)
        if (nextFilter != null) {
            selectNextFilter(nextFilter)
        } else {
            if (latestFilter.status == UNREAD) selectArticleFilter()
            onArticlesCleared()
        }
    }

    private fun selectNextFilter(filter: NextFilter) {
        when (filter) {
            is NextFilter.FeedFilter -> selectFeed(feedID = filter.feedID, folderTitle = filter.folderTitle)
            is NextFilter.FolderFilter -> selectFolder(title = filter.folderTitle)
            is NextFilter.SearchFilter -> selectSavedSearch(filter.savedSearchID)
        }
    }

    private suspend fun markRead(articleID: String) {
        account.markRead(articleID).onFailure { Sync.markReadAsync(listOf(articleID), context) }
        notificationHelper.dismissNotifications(listOf(articleID))
    }

    private suspend fun markUnread(articleID: String) {
        account.markUnread(articleID).onFailure { Sync.markUnreadAsync(articleID, context) }
    }

    private suspend fun addStar(articleID: String) {
        account.addStar(articleID).onFailure { Sync.addStarAsync(articleID, context) }
    }

    private suspend fun removeStar(articleID: String) {
        account.removeStar(articleID).onFailure { Sync.removeStarAsync(articleID, context) }
    }

    private val latestFilter: ArticleFilter get() = filter.value
    private val context: Context get() = application.applicationContext

    enum class UnauthorizedMessageState { HIDE, SHOW, LATER }
}

fun Context.showFullContentErrorToast(throwable: Throwable) {
    val message = when {
        throwable is ArticleContent.HttpError && throwable.code == 403 ->
            R.string.full_content_error_forbidden

        throwable is ArticleContent.MissingBodyError ->
            R.string.full_content_error_missing_response

        else -> R.string.full_content_error_generic
    }

    toast(message)
}

fun countableStatus(filter: ArticleFilter): ArticleStatus {
    return if (filter.status == STARRED) {
        STARRED
    } else {
        UNREAD
    }
}
