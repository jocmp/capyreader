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
import com.capyreader.app.sync.addStarAsync
import com.capyreader.app.sync.markReadAsync
import com.capyreader.app.sync.markUnreadAsync
import com.capyreader.app.sync.removeStarAsync
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.buildArticlePager
import com.jocmp.capy.common.UnauthorizedError
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

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleScreenViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
    private val application: Application,
) : AndroidViewModel(application) {
    private var refreshJob: Job? = null

    val filter = MutableStateFlow(appPreferences.filter.get())

    private val _searchQuery = MutableStateFlow<String?>(null)

    private var _article by mutableStateOf(account.findArticle(appPreferences.articleID.get()))

    private var _showUnauthorizedMessage by mutableStateOf(UnauthorizedMessageState.HIDE)

    private val _counts = filter.flatMapLatest { latestFilter ->
        account.countAll(latestFilter.status)
    }

    val articles: Flow<PagingData<Article>> = filter
        .combine(_searchQuery) { latestFilter, query ->
            account.buildArticlePager(filter = latestFilter, query = query).flow
        }
        .flatMapLatest { it }

    val folders: Flow<List<Folder>> = account.folders.combine(_counts) { folders, latestCounts ->
        folders.map { copyFolderCounts(it, latestCounts) }
            .withPositiveCount(filterStatus)
    }

    val allFeeds = account.allFeeds

    val feeds = account.feeds.combine(_counts) { feeds, latestCounts ->
        feeds.map { copyFeedCounts(it, latestCounts) }
            .withPositiveCount(filterStatus)
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

    private val filterStatus: ArticleStatus
        get() = filter.value.status

    fun selectArticleFilter() {
        val nextFilter = ArticleFilter.default().withStatus(status = filterStatus)

        selectArticleFilter(nextFilter)
    }

    fun selectStatus(status: ArticleStatus) {
        val nextFilter = filter.value.withStatus(status = status)

        updateFilterValue(nextFilter)
    }

    suspend fun selectFeed(feedID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val feed = account.findFeed(feedID) ?: return@launch
            val feedFilter = ArticleFilter.Feeds(feedID = feed.id, feedStatus = filter.value.status)

            selectArticleFilter(feedFilter)
        }
    }

    fun selectFolder(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = account.findFolder(title) ?: return@launch
            val feedFilter =
                ArticleFilter.Folders(
                    folderTitle = folder.title,
                    folderStatus = filter.value.status
                )

            selectArticleFilter(feedFilter)
        }
    }

    fun markAllRead(range: MarkRead) {
        viewModelScope.launch(Dispatchers.IO) {
            val articleIDs = account.unreadArticleIDs(filter = filter.value, range = range)

            markReadAsync(articleIDs, context)
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

            onComplete()
        }
    }

    fun selectArticle(articleID: String, completion: (article: Article) -> Unit) {
        viewModelScope.launch {
            _article = account.findArticle(articleID = articleID)?.copy(read = true)
            _article?.let(completion)
            appPreferences.articleID.set(articleID)
            markRead(articleID)
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

        viewModelScope.launch {
            appPreferences.articleID.delete()
        }
    }

    fun clearSearch() {
        _searchQuery.value = null
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    private suspend fun addStar(articleID: String) {
        account.addStar(articleID)
            .onFailure {
                addStarAsync(articleID, context)
            }
    }

    private suspend fun removeStar(articleID: String) {
        account.removeStar(articleID)
            .onFailure {
                removeStarAsync(articleID, context)
            }
    }

    private suspend fun markRead(articleID: String) {
        account.markRead(articleID)
            .onFailure {
                markReadAsync(listOf(articleID), context)
            }
    }

    private suspend fun markUnread(articleID: String) {
        account.markUnread(articleID)
            .onFailure {
                markUnreadAsync(articleID, context)
            }
    }

    private fun resetToDefaultFilter() {
        selectArticleFilter(ArticleFilter.default().copy(filterStatus))
    }

    private fun updateFilterValue(nextFilter: ArticleFilter) {
        filter.value = nextFilter
        appPreferences.filter.set(nextFilter)
    }

    private fun selectArticleFilter(nextFilter: ArticleFilter) {
        updateFilterValue(nextFilter)

        clearArticle()
    }

    private fun copyFolderCounts(folder: Folder, counts: Map<String, Long>): Folder {
        val folderFeeds = folder.feeds.map { copyFeedCounts(it, counts) }

        return folder.copy(
            feeds = folderFeeds.withPositiveCount(filterStatus).toMutableList(),
            count = folderFeeds.sumOf { it.count }
        )
    }

    private fun copyFeedCounts(feed: Feed, counts: Map<String, Long>): Feed {
        return feed.copy(count = counts.getOrDefault(feed.id, 0))
    }

    private val context: Context
        get() = application.applicationContext

    enum class UnauthorizedMessageState {
        HIDE,
        SHOW,
        LATER,
    }
}
