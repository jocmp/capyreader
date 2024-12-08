package com.capyreader.app.ui.articles

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.sync.Sync
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.buildArticlePager
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.countAll
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

    val statusCount: Flow<Long> = _counts.map {
        it.values.sum()
    }

    val showUnauthorizedMessage: Boolean
        get() = _showUnauthorizedMessage == UnauthorizedMessageState.SHOW

    val article: Article?
        get() = _article

    val searchQuery: Flow<String?>
        get() = _searchQuery

    fun selectArticleFilter() {
        val nextFilter = ArticleFilter.default().withStatus(status = latestFilter.status)

        updateFilter(nextFilter)
    }

    fun selectStatus(status: ArticleStatus) {
        val nextFilter = latestFilter.withStatus(status = status)

        updateFilter(nextFilter)
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

    fun markAllRead(range: MarkRead) {
        viewModelScope.launchIO {
            val articleIDs = account.unreadArticleIDs(
                filter = latestFilter,
                range = range,
                unreadSort = unreadSort.value
            )

            account.markAllRead(articleIDs).onFailure {
                Sync.markReadAsync(articleIDs, context)
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

    fun onRequestNextFeed(feeds: List<Feed>, folders: List<Folder>) = viewModelScope.launchIO {
        val currentFilter = filter.value

        when (currentFilter) {
            is ArticleFilter.Articles -> {
                val firstFeed = feeds.firstOrNull()
                val firstFolder = folders.firstOrNull()

                if (firstFolder != null) {
                    selectFolder(firstFolder.title)
                } else if (firstFeed != null) {
                    selectFeed(feedID = firstFeed.id, folderTitle = null)
                }
            }

            is ArticleFilter.Folders -> {
                val firstFeed = folders
                    .find { it.title == currentFilter.folderTitle }
                    ?.feeds
                    ?.firstOrNull()

                if (firstFeed != null) {
                    selectFeed(feedID = firstFeed.id, folderTitle = currentFilter.folderTitle)
                }
            }

            is ArticleFilter.Feeds -> TODO()
        }
        // if filter is folder, select first item in folder
        // if filter is feed, select next feed
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

    private fun updateFilter(nextFilter: ArticleFilter) {
        appPreferences.filter.set(nextFilter)

        updateArticlesSince()

        clearArticle()
    }

    private fun updateArticlesSince() {
        articlesSince.value = OffsetDateTime.now()
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
        account.fetchFullContent(article).fold(
            onSuccess = { value ->
                if (_article?.id == article.id) {
                    _article = article.copy(
                        content = value,
                        fullContent = Article.FullContentState.LOADED
                    )
                }
            },
            onFailure = { resetFullContent() }
        )
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
