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
import com.capyreader.app.common.AfterReadAllBehavior
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.toast
import com.capyreader.app.sync.Sync
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.articles.NextFilter
import com.jocmp.capy.buildArticlePager
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import com.jocmp.capy.countAll
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.Dispatchers
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
) : AndroidViewModel(application) {
    private var refreshJob: Job? = null

    val filter = appPreferences.filter.stateIn(viewModelScope)

    private val _searchQuery = MutableStateFlow<String?>(null)

    private var _article by mutableStateOf<Article?>(null)

    private val articlesSince = MutableStateFlow<OffsetDateTime>(OffsetDateTime.now())

    private var _showUnauthorizedMessage by mutableStateOf(UnauthorizedMessageState.HIDE)

    private val unreadSort = appPreferences.articleListOptions.unreadSort.stateIn(viewModelScope)

    val afterReadAll =
        appPreferences.articleListOptions.afterReadAllBehavior.stateIn(viewModelScope)

    private val _counts = filter.flatMapLatest { latestFilter ->
        account.countAll(latestFilter.status)
    }

    val articles: Flow<PagingData<Article>> =
        combine(
            filter,
            _searchQuery,
            articlesSince,
            unreadSort
        ) { filter, query, since, sort ->
            account.buildArticlePager(
                filter = filter,
                query = query,
                unreadSort = sort,
                since = since
            ).flow
        }.flatMapLatest { it }

    val folders: Flow<List<Folder>> = combine(
        account.folders,
        _counts,
        filter,
    ) { folders, latestCounts, filter ->
        folders.map { copyFolderCounts(it, latestCounts, filter) }
            .withPositiveCount(filter.status)
    }

    val allFeeds = account.allFeeds

    val allFolders = account.folders

    val feeds = combine(
        account.feeds,
        _counts,
        filter,
    ) { feeds, latestCounts, filter ->
        feeds.map { copyFeedCounts(it, latestCounts) }
            .withPositiveCount(filter.status)
    }

    private val nextFilterListener: Flow<NextFilter?> =
        combine(feeds, folders, filter) { feeds, folders, filter ->
            NextFilter.findSwipeDestination(filter, feeds, folders)
        }

    private val _nextFilter = MutableStateFlow<NextFilter?>(null)

    val statusCount: Flow<Long> = _counts.map {
        it.values.sum()
    }

    val showUnauthorizedMessage: Boolean
        get() = _showUnauthorizedMessage == UnauthorizedMessageState.SHOW

    val article: Article?
        get() = _article

    val searchQuery: Flow<String?>
        get() = _searchQuery

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
        viewModelScope.launch(Dispatchers.IO) {
            val feed = account.findFeed(feedID) ?: return@launch
            val feedFilter = ArticleFilter.Feeds(
                feedID = feed.id,
                folderTitle = folderTitle,
                feedStatus = latestFilter.status
            )

            updateFilter(feedFilter)
        }
    }

    fun selectFolder(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = account.findFolder(title) ?: return@launch
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
        feeds: List<Feed>,
        folders: List<Folder>,
    ) {
        viewModelScope.launchIO {
            val articleIDs = account.unreadArticleIDs(
                filter = latestFilter,
                range = range,
                unreadSort = unreadSort.value
            )

            account.markAllRead(articleIDs).onFailure {
                Sync.markReadAsync(articleIDs, context)
            }

            if (range != MarkRead.All) {
                return@launchIO
            }

            if (afterReadAll.value == AfterReadAllBehavior.HIDE_ARTICLES) {
                clearArticlesOnAllRead(onArticlesCleared)
            } else if (afterReadAll.value == AfterReadAllBehavior.OPEN_NEXT_FEED) {
                openNextFeedOnAllRead(onArticlesCleared, feeds, folders)
            }
        }
    }

    fun removeFeed(
        feedID: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            account.removeFeed(feedID = feedID).fold(
                onSuccess = {
                    resetToDefaultFilter()
                    onSuccess()
                },
                onFailure = {
                    onFailure()
                }
            )
        }
    }

    fun refreshFeed(onComplete: () -> Unit) {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            account.refresh().onFailure { throwable ->
                if (throwable is UnauthorizedError && _showUnauthorizedMessage == UnauthorizedMessageState.HIDE) {
                    _showUnauthorizedMessage = UnauthorizedMessageState.SHOW
                }
            }

            updateArticlesSince()

            onComplete()
        }
    }

    fun selectArticle(articleID: String) {
        if (_article?.id == articleID) {
            return
        }

        viewModelScope.launchIO {
            val article = buildArticle(articleID) ?: return@launchIO
            _article = article

            markRead(articleID)

            if (article.fullContent == Article.FullContentState.LOADING) {
                viewModelScope.launch(Dispatchers.IO) { fetchFullContent(article) }
            }

            appPreferences.articleID.set(articleID)
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

    fun clearSearch() {
        _searchQuery.value = null
    }

    fun updateSearch(query: String) {
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

        updateArticlesSince()

        clearArticle()
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

    private fun clearArticlesOnAllRead(
        onArticlesCleared: () -> Unit,
    ) {
        updateArticlesSince()
        onArticlesCleared()
    }

    private fun openNextFeedOnAllRead(
        onArticlesCleared: () -> Unit,
        feeds: List<Feed>,
        folders: List<Folder>,
    ) {
        val nextFilter = NextFilter.findMarkReadDestination(
            latestFilter,
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
