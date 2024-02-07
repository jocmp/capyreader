package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.AddFeedForm
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Countable
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.buildPager
import com.jocmp.basil.countAll
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AccountViewModel(
    accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val accountState: MutableState<Account> = mutableStateOf(
        accountManager.findByID(appPreferences.accountID.get())!!,
        policy = neverEqualPolicy()
    )

    private val _counts = mutableStateOf<Map<String, Long>>(mapOf())

    private val _filter = mutableStateOf(appPreferences.filter.get())

    private val pager = mutableStateOf(account.buildPager(_filter.value))

    private val _articles = derivedStateOf { pager.value.flow.cachedIn(viewModelScope) }

    init {
        refreshCounts()
    }

    val articles: Flow<PagingData<Article>>
        get() = _articles.value

    private val account: Account
        get() = accountState.value

    val folders: List<Folder>
        get() = account.folders.map(::copyFolderUnreadCounts).withPositiveCount(filterStatus)

    private val articleState = mutableStateOf(account.findArticle(appPreferences.articleID.get()))

    val statusCount: Long
        get() = _counts.value.values.sum()

    val feeds: List<Feed>
        get() = account.feeds.map(::copyFeedUnreadCounts).withPositiveCount(filterStatus)

    val article: Article?
        get() = articleState.value

    val filterStatus: ArticleStatus
        get() = _filter.value.status

    val feed: Feed?
        get() = (_filter.value as? ArticleFilter.Feeds)?.feed

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
        val feed = account.findFeed(feedID) ?: return
        val feedFilter = ArticleFilter.Feeds(feed = feed, feedStatus = _filter.value.status)

        selectArticleFilter(feedFilter)
    }

    fun selectFolder(title: String) {
        val folder = account.findFolder(title) ?: return
        val feedFilter = ArticleFilter.Folders(folder = folder, folderStatus = _filter.value.status)

        selectArticleFilter(feedFilter)
    }

    fun removeFeed(feedID: String) {
        viewModelScope.launch {
            account.removeFeed(feedID = feedID)
            resetToDefaultFilter()
            accountState.value = account
        }
    }

    fun removeFolder(folderTitle: String) {
        viewModelScope.launch {
            account.removeFolder(title = folderTitle)
            resetToDefaultFilter()
            accountState.value = account
        }
    }

    suspend fun refreshFeed() {
        when (val currentFilter = filter) {
            is ArticleFilter.Feeds -> account.refreshFeed(currentFilter.feed)
            is ArticleFilter.Folders -> account.refreshFeeds(currentFilter.folder.feeds)
            is ArticleFilter.Articles -> account.refreshAll()
        }

        refreshCounts()
    }

    fun selectArticle(articleID: String, onSuccess: (article: Article) -> Unit) {
        account.markRead(articleID)
        articleState.value = account.findArticle(articleID = articleID)
        articleState.value?.let(onSuccess)

        viewModelScope.launch {
            appPreferences.articleID.set(articleID)
        }

        refreshCounts()
    }

    fun toggleArticleRead() {
        articleState.value?.let { article ->
            if (article.read) {
                account.markUnread(article.id)
            } else {
                account.markRead(article.id)
            }

            articleState.value = article.copy(read = !article.read)

            refreshCounts()
        }
    }

    fun toggleArticleStar() {
        articleState.value?.let { article ->
            if (article.starred) {
                account.removeStar(article.id)
            } else {
                account.addStar(article.id)
            }

            articleState.value = article.copy(starred = !article.starred)

            refreshCounts()
        }
    }

    fun clearArticle() {
        articleState.value = null

        viewModelScope.launch {
            appPreferences.articleID.delete()
        }
    }

    fun addFeed(
        entry: AddFeedForm,
        onSuccess: () -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            account.addFeed(entry).fold(
                onSuccess = { feed ->
                    selectFeed(feed.id)
                    onSuccess()
                },
                onFailure = {
                    onFailure(it.message ?: "")
                }
            )
        }
    }

    private fun resetToDefaultFilter() {
        selectArticleFilter(ArticleFilter.default().copy(filter.status))
    }

    private fun updateFilterValue(nextFilter: ArticleFilter) {
        _filter.value = nextFilter
        pager.value = account.buildPager(nextFilter)

        viewModelScope.launch {
            appPreferences.filter.set(nextFilter)
            refreshCounts()
        }
    }

    private fun selectArticleFilter(nextFilter: ArticleFilter) {
        updateFilterValue(nextFilter)

        clearArticle()
    }

    private fun copyFolderUnreadCounts(folder: Folder): Folder {
        val folderFeeds = folder.feeds.map(::copyFeedUnreadCounts)

        return folder.copy(
            feeds = folderFeeds.withPositiveCount(filterStatus).toMutableList(),
            count = folderFeeds.sumOf { it.count }
        )
    }

    private fun copyFeedUnreadCounts(feed: Feed): Feed {
        return feed.copy(count = _counts.value.getOrDefault(feed.id, 0))
    }

    private fun refreshCounts() {
        _counts.value = accountState.value.countAll(status = filterStatus)
    }
}

private fun <T : Countable> List<T>.withPositiveCount(status: ArticleStatus): List<T> {
    return filter { status == ArticleStatus.ALL || it.count > 0 }
}
