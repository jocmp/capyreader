package com.jocmp.capyreader.ui.articles

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Countable
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.buildPager
import com.jocmp.capy.countAll
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.sync.addStarAsync
import com.jocmp.capyreader.sync.markReadAsync
import com.jocmp.capyreader.sync.markUnreadAsync
import com.jocmp.capyreader.sync.removeStarAsync
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

    private val articleState = mutableStateOf(account.findArticle(appPreferences.articleID.get()))

    private val _counts = filter.flatMapLatest { latestFilter ->
        account.countAll(latestFilter.status)
    }

    val articles: Flow<PagingData<Article>> = filter
        .flatMapLatest { account.buildPager(it).flow }
        .cachedIn(viewModelScope)

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

    val article: Article?
        get() = articleState.value

    private val filterStatus: ArticleStatus
        get() = filter.value.status

    fun selectArticleFilter() {
        val nextFilter = ArticleFilter.default().withStatus(status = filterStatus)

        updateFilterValue(nextFilter)
    }

    fun selectStatus(status: ArticleStatus) {
        val nextFilter = filter.value.withStatus(status = status)

        updateFilterValue(nextFilter)
    }

    suspend fun selectFeed(feedID: String) {
        val feed = account.findFeed(feedID) ?: return
        val feedFilter = ArticleFilter.Feeds(feedID = feed.id, feedStatus = filter.value.status)

        selectArticleFilter(feedFilter)
    }

    fun selectFolder(title: String) {
        viewModelScope.launch {
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
            account.refresh()

            onComplete()
        }
    }

    fun selectArticle(articleID: String, completion: (article: Article) -> Unit) {
        viewModelScope.launch {
            articleState.value = account.findArticle(articleID = articleID)?.copy(read = true)
            articleState.value?.let(completion)
            appPreferences.articleID.set(articleID)
            markRead(articleID)
        }
    }

    fun toggleArticleRead() {
        articleState.value?.let { article ->
            viewModelScope.launch {
                if (article.read) {
                    markUnread(article.id)
                } else {
                    markRead(article.id)
                }
            }

            articleState.value = article.copy(read = !article.read)
        }
    }

    fun toggleArticleStar() {
        articleState.value?.let { article ->
            viewModelScope.launch {
                if (article.starred) {
                    removeStar(article.id)
                } else {
                    addStar(article.id)
                }

                articleState.value = article.copy(starred = !article.starred)
            }
        }
    }

    fun clearArticle() {
        articleState.value = null

        viewModelScope.launch {
            appPreferences.articleID.delete()
        }
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
}

private fun <T : Countable> List<T>.withPositiveCount(status: ArticleStatus): List<T> {
    return filter { status == ArticleStatus.ALL || it.count > 0 }
}
