package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jocmp.basil.Account
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Countable
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.buildPager
import com.jocmp.basil.countAll
import com.jocmp.basilreader.common.AppPreferences
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
class AccountViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
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
                ArticleFilter.Folders(folderTitle = folder.title, folderStatus = filter.value.status)

            selectArticleFilter(feedFilter)
        }
    }

    fun removeFeed(feedID: String) {
        viewModelScope.launch {
            account.removeFeed(feedID = feedID)
            resetToDefaultFilter()
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
            account.markRead(articleID)
        }
    }

    fun toggleArticleRead() {
        articleState.value?.let { article ->
            viewModelScope.launch {
                if (article.read) {
                    account.markUnread(article.id)
                } else {
                    account.markRead(article.id)
                }
            }

            articleState.value = article.copy(read = !article.read)
        }
    }

    fun toggleArticleStar() {
        articleState.value?.let { article ->
            viewModelScope.launch {
                if (article.starred) {
                    account.removeStar(article.id)
                } else {
                    account.addStar(article.id)
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
}

private fun <T : Countable> List<T>.withPositiveCount(status: ArticleStatus): List<T> {
    return filter { status == ArticleStatus.ALL || it.count > 0 }
}
