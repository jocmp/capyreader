package com.capyreader.app.ui.articles

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
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
import com.jocmp.capy.ArticlePages
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.articles.NextFilter
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import com.jocmp.capy.countAll
import com.jocmp.capy.findArticlePages
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticlePagerFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    private var fullContentJob: Job? = null

    val filter = appPreferences.filter.stateIn(viewModelScope)

    private val listSwipeBottom =
        appPreferences.articleListOptions.swipeBottom.stateIn(viewModelScope)

    private val _searchQuery = MutableStateFlow("")

    private val _searchState = MutableStateFlow(SearchState.INACTIVE)

    private var _article by mutableStateOf<Article?>(null)

    var refreshingAll by mutableStateOf(false)
        private set

    val articlesSince = MutableStateFlow<OffsetDateTime>(OffsetDateTime.now())

    private var _showUnauthorizedMessage by mutableStateOf(UnauthorizedMessageState.HIDE)

    val unreadSort = appPreferences.articleListOptions.unreadSort.stateIn(viewModelScope)

    val afterReadAll =
        appPreferences.articleListOptions.afterReadAllBehavior.stateIn(viewModelScope)

    private val _counts = filter.flatMapLatest { latestFilter ->
        account.countAll(latestFilter.status)
    }

    fun pager(
        filter: ArticleFilter,
        sort: UnreadSortOrder,
        query: String = "",
    ): Pager<Int, Article> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 10,
            ),
            pagingSourceFactory = {
                val since = articlesSince.value

                ArticlePagerFactory(account).findArticles(
                    filter = filter,
                    query = query,
                    unreadSort = sort,
                    since = since
                )
            }
        )
    }

    val folders: Flow<List<Folder>> = combine(
        account.folders,
        _counts,
        filter,
    ) { folders, latestCounts, filter ->
        folders.map { copyFolderCounts(it, latestCounts, filter) }
            .withPositiveCount(filter.status)
    }

    val savedSearches = account.savedSearches

    val allFeeds = account.taggedFeeds

    val showOnboarding = allFeeds.map { it.isEmpty() }

    val allFolders = account.folders

    val topLevelFeeds = combine(
        account.feeds,
        _counts,
        filter,
    ) { feeds, latestCounts, filter ->
        feeds.map { copyFeedCounts(it, latestCounts) }
            .withPositiveCount(filter.status)
    }

    val currentFeed: Flow<Feed?> = combine(allFeeds, filter) { feeds, filter ->
        if (filter is ArticleFilter.Feeds) {
            feeds.find { it.id == filter.feedID }
        } else {
            null
        }
    }

    private val nextFilterListener: Flow<NextFilter?> =
        combine(
            listSwipeBottom,
            savedSearches,
            topLevelFeeds,
            folders,
            filter
        ) { swipeBottom, savedSearches, feeds, folders, filter ->
            if (swipeBottom == ArticleListVerticalSwipe.DISABLED) {
                return@combine null
            }

            NextFilter.findSwipeDestination(
                filter,
                searches = savedSearches,
                folders = folders,
                feeds = feeds,
            )
        }

    private val _nextFilter = MutableStateFlow<NextFilter?>(null)

    val statusCount: Flow<Long> = _counts.map {
        it.values.sum()
    }

    val showUnauthorizedMessage: Boolean
        get() = _showUnauthorizedMessage == UnauthorizedMessageState.SHOW

    val article: Article?
        get() = _article

    val searchQuery: Flow<String>
        get() = _searchQuery

    val searchState: Flow<SearchState>
        get() = _searchState

    val nextFilter: Flow<NextFilter?>
        get() = _nextFilter

    init {
        viewModelScope.launch {
            nextFilterListener.collect {
                _nextFilter.value = it
            }
        }
    }

    fun selectArticleFilter() {
        val filter = ArticleFilter.default().withStatus(status = latestFilter.status)

        updateFilter(filter)
    }

    fun selectStatus(status: ArticleStatus) {
        val filter = latestFilter.withStatus(status = status)

        updateFilter(filter)
    }

    fun selectFeed(feedID: String, folderTitle: String? = null) {
        viewModelScope.launchIO {
            val feed = account.findFeed(feedID) ?: return@launchIO
            val feedFilter = ArticleFilter.Feeds(
                feedID = feed.id,
                folderTitle = folderTitle,
                feedStatus = latestFilter.status
            )

            updateFilter(feedFilter)
        }
    }

    fun selectSavedSearch(savedSearchID: String) {
        viewModelScope.launchIO {
            val savedSearch = account.findSavedSearch(savedSearchID) ?: return@launchIO
            val searchFilter = ArticleFilter.SavedSearches(
                savedSearch.id,
                savedSearchStatus = latestFilter.status
            )

            updateFilter(searchFilter)
        }
    }

    fun selectFolder(title: String) {
        viewModelScope.launchIO {
            val folder = account.findFolder(title) ?: return@launchIO
            val feedFilter =
                ArticleFilter.Folders(
                    folderTitle = folder.title,
                    folderStatus = latestFilter.status
                )

            updateFilter(feedFilter)
        }
    }

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
                unreadSort = unreadSort.value,
                query = _searchQuery.value,
            )

            account.markAllRead(articleIDs).onFailure {
                Sync.markReadAsync(articleIDs, context)
            }

            launchIO {
                notificationHelper.dismissNotifications(articleIDs)
            }

            if (range != MarkRead.All) {
                return@launchIO
            }

            if (afterReadAll.value == AfterReadAllBehavior.OPEN_DRAWER) {
                onArticlesCleared()
            } else if (afterReadAll.value == AfterReadAllBehavior.OPEN_NEXT_FEED) {
                openNextFeedOnAllRead(onArticlesCleared, searches, folders, feeds)
            }
        }
    }

    fun removeFeed(
        feedID: String,
        completion: (result: Result<Unit>) -> Unit
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

    private fun refreshFilter(
        filter: ArticleFilter,
        onComplete: () -> Unit,
    ) {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launchIO {
            account.refresh(filter).onFailure { throwable ->
                if (throwable is UnauthorizedError && _showUnauthorizedMessage == UnauthorizedMessageState.HIDE) {
                    _showUnauthorizedMessage = UnauthorizedMessageState.SHOW
                }
            }

            launchIO {
                WidgetUpdater.update(context)
            }

            onComplete()
        }
    }

    fun refresh(filter: ArticleFilter, onComplete: () -> Unit) {
        refreshFilter(filter) {
            updateArticlesSince()
            onComplete()
        }
    }

    fun refreshAll(onComplete: () -> Unit) {
        refreshingAll = true

        refresh(ArticleFilter.default()) {
            onComplete()

            refreshJob?.invokeOnCompletion {
                refreshingAll = false
            }
        }
    }

    fun selectArticle(articleID: String, onComplete: (article: Article) -> Unit = {}) {
        if (_article?.id == articleID) {
            return
        }

        viewModelScope.launchIO {
            val article = buildArticle(articleID) ?: return@launchIO
            _article = article

            appPreferences.articleID.set(articleID)

            launchIO {
                markRead(articleID)
            }

            launchUI {
                onComplete(article)
            }

            if (article.fullContent == Article.FullContentState.LOADING) {
                fullContentJob?.cancel()

                fullContentJob = viewModelScope.launchIO { fetchFullContent(article) }
            }
        }
    }

    fun toggleArticleRead() {
        _article?.let { article ->
            viewModelScope.launch {
                if (article.read) {
                    markUnread(article.id)
                } else {
                    markRead(article.id)
                }
            }

            _article = article.copy(read = !article.read)
        }
    }

    fun toggleArticleStar() {
        _article?.let { article ->
            viewModelScope.launch {
                if (article.starred) {
                    removeStar(article.id)
                } else {
                    addStar(article.id)
                }

                _article = article.copy(starred = !article.starred)
            }
        }
    }

    fun dismissUnauthorizedMessage() {
        _showUnauthorizedMessage = UnauthorizedMessageState.LATER
    }

    fun clearArticle() {
        _article = null
        appPreferences.articleID.delete()
    }

    fun startSearch() {
        _searchState.value = SearchState.ACTIVE
    }

    fun clearSearch() {
        if (_searchQuery.value.isNotBlank()) {
            clearArticle()
        }
        _searchQuery.value = ""
        _searchState.value = SearchState.INACTIVE
    }

    fun updateSearch(query: String) {
        clearArticle()
        _searchQuery.value = query
    }

    fun addStarAsync(articleID: String) {
        toggleCurrentStarred(articleID)
        addStar(articleID)
    }

    fun removeStarAsync(articleID: String) = viewModelScope.launchIO {
        toggleCurrentStarred(articleID)
        removeStar(articleID)
    }

    fun markReadAsync(articleID: String) = viewModelScope.launchIO {
        toggleCurrentRead(articleID)
        markRead(articleID)
    }

    fun markUnreadAsync(articleID: String) = viewModelScope.launchIO {
        toggleCurrentRead(articleID)
        markUnread(articleID)
    }

    fun requestNextFeed() {
        _nextFilter.value?.let(::selectNextFilter)
    }

    private fun selectNextFilter(filter: NextFilter) {
        when (filter) {
            is NextFilter.FeedFilter -> selectFeed(
                feedID = filter.feedID,
                folderTitle = filter.folderTitle
            )

            is NextFilter.FolderFilter -> selectFolder(title = filter.folderTitle)
            is NextFilter.SearchFilter -> selectSavedSearch(filter.savedSearchID)
        }
    }

    private fun addStar(articleID: String) {
        viewModelScope.launchIO {
            account.addStar(articleID)
                .onFailure {
                    Sync.addStarAsync(articleID, context)
                }
        }
    }

    private suspend fun removeStar(articleID: String) {
        account.removeStar(articleID)
            .onFailure {
                Sync.removeStarAsync(articleID, context)
            }
    }

    private suspend fun markRead(articleID: String) {
        account.markRead(articleID)
            .onFailure {
                Sync.markReadAsync(listOf(articleID), context)
            }

        notificationHelper.dismissNotifications(listOf(articleID))
    }

    private suspend fun markUnread(articleID: String) {
        account.markUnread(articleID)
            .onFailure {
                Sync.markUnreadAsync(articleID, context)
            }
    }

    private fun resetToDefaultFilter() {
        updateFilter(ArticleFilter.default().copy(latestFilter.status))
    }

    private fun toggleCurrentStarred(articleID: String) {
        _article?.let { article ->
            if (articleID == article.id) {
                _article = article.copy(starred = !article.starred)
            }
        }
    }

    private fun toggleCurrentRead(articleID: String) {
        _article?.let { article ->
            if (articleID == article.id) {
                _article = article.copy(read = !article.read)
            }
        }
    }

    private fun updateFilter(filter: ArticleFilter) {
        appPreferences.filter.set(filter)

        clearArticle()

        updateArticlesSince()
    }

    private fun updateArticlesSince() {
        articlesSince.value = OffsetDateTime.now().plusSeconds(1)
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

    private fun buildArticle(articleID: String): Article? {
        val article = account.findArticle(articleID = articleID) ?: return null

        val fullContent = if (enableStickyFullContent && article.enableStickyFullContent) {
            Article.FullContentState.LOADING
        } else {
            Article.FullContentState.NONE
        }

        val content = if (fullContent == Article.FullContentState.LOADING) {
            ""
        } else {
            article.defaultContent
        }

        return article.copy(
            read = true,
            content = content,
            fullContent = fullContent
        )
    }

    fun fetchFullContentAsync(article: Article? = _article) {
        article ?: return

        viewModelScope.launchIO {
            if (enableStickyFullContent && !account.isFullContentEnabled(feedID = article.feedID)) {
                account.enableStickyContent(article.feedID)
            }

            _article = article.copy(fullContent = Article.FullContentState.LOADING)

            _article?.let { fetchFullContent(it) }
        }
    }

    fun resetFullContent() {
        val article = _article ?: return

        _article = article.copy(
            content = article.defaultContent,
            fullContent = Article.FullContentState.NONE
        )

        if (enableStickyFullContent) {
            account.disableStickyContent(article.feedID)
        }
    }

    fun findArticlePages(articleID: String): Flow<ArticlePages?> {
        return account.findArticlePages(
            articleID = articleID,
            filter = latestFilter,
            query = _searchQuery.value,
            unreadSort = unreadSort.value,
            since = articlesSince.value
        )
    }

    private suspend fun fetchFullContent(article: Article) {
        account.fetchFullContent(article)
            .fold(
                onSuccess = { value ->
                    if (_article?.id == article.id) {
                        _article = article.copy(
                            content = value,
                            fullContent = Article.FullContentState.LOADED
                        )
                    }
                },
                onFailure = {
                    if (_article?.id != article.id) {
                        return
                    }
                    _article = article.copy(
                        content = article.defaultContent,
                        fullContent = Article.FullContentState.ERROR
                    )

                    CapyLog.warn(
                        "full_content",
                        mapOf(
                            "error_type" to it::class.simpleName,
                            "error_message" to it.message
                        )
                    )

                    viewModelScope.launchUI {
                        context.showFullContentErrorToast(it)
                    }
                }
            )
    }

    private fun openNextFeedOnAllRead(
        onArticlesCleared: () -> Unit,
        searches: List<SavedSearch>,
        folders: List<Folder>,
        feeds: List<Feed>,
    ) {
        val nextFilter = NextFilter.findMarkReadDestination(
            latestFilter,
            searches,
            folders,
            feeds,
        )

        if (nextFilter != null) {
            selectNextFilter(nextFilter)
        } else {
            if (latestFilter.status == ArticleStatus.UNREAD) {
                selectArticleFilter()
            }
            onArticlesCleared()
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

    private val latestFilter: ArticleFilter
        get() = filter.value

    private val enableStickyFullContent: Boolean
        get() = appPreferences.enableStickyFullContent.get()
    private val context: Context
        get() = application.applicationContext

    enum class UnauthorizedMessageState {
        HIDE,
        SHOW,
        LATER,
    }
}

fun Context.showFullContentErrorToast(throwable: Throwable) {
    val message = when (throwable) {
        is ArticleContent.MissingBodyError -> R.string.full_content_error_missing_response
        else -> R.string.full_content_error_generic
    }

    toast(message)
}
