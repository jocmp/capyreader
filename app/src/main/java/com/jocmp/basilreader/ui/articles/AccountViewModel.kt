package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Countable
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.buildPager
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AccountViewModel(
    accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _account = accountManager.findByID(appPreferences.accountID.get())!!

    private val _counts = mutableStateOf<Map<String, Long>>(mapOf())

    private val _filter = mutableStateOf(appPreferences.filter.get())

    private val pager = mutableStateOf(account.buildPager(_filter.value))

    private val _articles = derivedStateOf { pager.value.flow.cachedIn(viewModelScope) }

    val articles: Flow<PagingData<Article>>
        get() = _articles.value

    private val account: Account
        get() = _account

    private val articleState = mutableStateOf(account.findArticle(appPreferences.articleID.get()))

    val statusCount: Long
        get() = _counts.value.values.sum()

    val folders = account.folders

    val feeds = account.feeds

    val article: Article?
        get() = articleState.value

    private val filterStatus: ArticleStatus
        get() = _filter.value.status

    val filter: ArticleFilter
        get() = _filter.value

    fun selectArticleFilter() {
        val nextFilter = ArticleFilter.default().withStatus(status = filterStatus)

        updateFilterValue(nextFilter)
    }

    fun selectStatus(status: ArticleStatus) {
        val nextFilter = _filter.value.withStatus(status = status)

        updateFilterValue(nextFilter)
    }

    fun selectFeed(feedID: String) {
        viewModelScope.launch {
            val feed = account.findFeed(feedID) ?: return@launch
            val feedFilter = ArticleFilter.Feeds(feed = feed, feedStatus = _filter.value.status)

            selectArticleFilter(feedFilter)
        }
    }

    fun selectFolder(title: String) {
        viewModelScope.launch {
            val folder = account.findFolder(title) ?: return@launch
            val feedFilter =
                ArticleFilter.Folders(folder = folder, folderStatus = _filter.value.status)

            selectArticleFilter(feedFilter)
        }
    }

    fun removeFeed(feedID: String) {
        viewModelScope.launch {
            account.removeFeed(feedID = feedID)
            resetToDefaultFilter()
        }
    }

    fun removeFolder(folderTitle: String) {
        viewModelScope.launch {
            account.removeFolder(title = folderTitle)
            resetToDefaultFilter()
        }
    }

    fun refreshFeed(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val currentFilter = filter) {
                is ArticleFilter.Feeds -> account.refreshFeed(currentFilter.feed)
                is ArticleFilter.Folders -> account.refreshFeeds(currentFilter.folder.feeds)
                is ArticleFilter.Articles -> account.refreshAll()
            }

            onSuccess()
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

    fun addFeed(
        url: String,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            val result = account.addFeed(url)

            if (result.isSuccess) {
                onComplete()
            }
        }
    }

    fun reload() {

    }

    private fun resetToDefaultFilter() {
        selectArticleFilter(ArticleFilter.default().copy(filter.status))
    }

    private fun updateFilterValue(nextFilter: ArticleFilter) {
        _filter.value = nextFilter
        pager.value = account.buildPager(nextFilter)
        appPreferences.filter.set(nextFilter)
    }

    private fun selectArticleFilter(nextFilter: ArticleFilter) {
        updateFilterValue(nextFilter)

        clearArticle()
    }

    private fun copyFolderCounts(folder: Folder): Folder {
        val folderFeeds = folder.feeds.map(::copyFeedCounts)

        return folder.copy(
            feeds = folderFeeds.withPositiveCount(filterStatus).toMutableList(),
            count = folderFeeds.sumOf { it.count }
        )
    }

    private fun copyFeedCounts(feed: Feed): Feed {
        return feed.copy(count = _counts.value.getOrDefault(feed.id, 0))
    }
}

private fun <T : Countable> List<T>.withPositiveCount(status: ArticleStatus): List<T> {
    return filter { status == ArticleStatus.ALL || it.count > 0 }
}
