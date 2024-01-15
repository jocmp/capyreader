package com.jocmp.basilreader.ui.accounts

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Feed
import com.jocmp.basil.AddFeedForm
import com.jocmp.basil.Folder
import com.jocmp.basil.buildPager
import com.jocmp.basil.unreadCounts
import com.jocmp.basilreader.articleID
import com.jocmp.basilreader.filter
import com.jocmp.basilreader.putArticleID
import com.jocmp.basilreader.putFilter
import com.jocmp.basilreader.selectedAccountID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AccountViewModel(
    accountManager: AccountManager,
    private val settings: DataStore<Preferences>,
) : ViewModel() {
    private val initialPreferences = runBlocking { settings.data.first() }

    private val accountState: MutableState<Account> = mutableStateOf(
        accountManager.findByID(initialPreferences.selectedAccountID)!!,
        policy = neverEqualPolicy()
    )

    private val _unreadCounts = mutableStateOf<Map<String, Long>>(mapOf())

    init {
        refreshUnreadCounts()
    }

    private val _filter = mutableStateOf(ArticleFilter.findOrDefault(initialPreferences))

    private val pager = mutableStateOf(account.buildPager(_filter.value))

    val articles: Flow<PagingData<Article>>
        get() = pager.value.flow

    private val account: Account
        get() = accountState.value

    val folders: List<Folder>
        get() = account.folders.map(::copyFolderUnreadCounts)

    private val articleState = mutableStateOf(account.findArticleOrNull(initialPreferences))

    val feeds: List<Feed>
        get() = account.feeds.map(::copyFeedUnreadCounts)

    val article: Article?
        get() = articleState.value

    val filterStatus: ArticleStatus
        get() = _filter.value.status

    val feed: Feed?
        get() = (_filter.value as? ArticleFilter.Feeds)?.feed

    val filter: ArticleFilter
        get() = _filter.value

    fun selectStatus(status: ArticleStatus) {
        val nextFilter = _filter.value.withStatus(status = status)

        updateFilterValue(nextFilter)
    }

    fun selectFeed(feedID: String) {
        val feed = account.findFeed(feedID) ?: return
        val feedFilter = ArticleFilter.Feeds(feed = feed, feedStatus = _filter.value.status)

        selectFilter(feedFilter)
    }

    fun selectFolder(title: String) {
        val folder = account.findFolder(title) ?: return
        val feedFilter = ArticleFilter.Folders(folder = folder, folderStatus = _filter.value.status)

        selectFilter(feedFilter)
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
    }

    fun selectArticle(articleID: String) {
        account.markRead(articleID)
        articleState.value = account.findArticle(articleID = articleID)

        viewModelScope.launch {
            settings.putArticleID(articleID)
        }

        refreshUnreadCounts()
    }

    fun toggleArticleRead() {
        articleState.value?.let { article ->
            if (article.read) {
                account.markUnread(article.id)
            } else {
                account.markRead(article.id)
            }

            articleState.value = article.copy(read = !article.read)

            refreshUnreadCounts()
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
        }
    }

    fun clearArticle() {
        articleState.value = null

        viewModelScope.launch {
            settings.putArticleID(null)
        }
    }

    fun addFeed(entry: AddFeedForm, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = account.addFeed(entry)

            result.onSuccess { feed ->
                selectFeed(feed.id)
                onSuccess()
            }
        }
    }

    private fun resetToDefaultFilter() {
        selectFilter(ArticleFilter.default().copy(filter.status))
    }

    private fun updateFilterValue(nextFilter: ArticleFilter) {
        _filter.value = nextFilter
        pager.value = account.buildPager(nextFilter)

        viewModelScope.launch {
            settings.putFilter(nextFilter)
        }
    }

    private fun selectFilter(nextFilter: ArticleFilter) {
        updateFilterValue(nextFilter)

        clearArticle()
    }

//    private fun selectSettingsAccount(accountID: String) {
//        viewModelScope.launch {
//            settings.selectAccount(accountID)
//        }
//    }
//
//    private fun selectAccount(accountID: String) {
//        accountManager.findByID(accountID)?.let {
//            accountState.value = it
//        }
//    }

    private fun copyFolderUnreadCounts(folder: Folder): Folder {
        val folderFeeds = folder.feeds.map(::copyFeedUnreadCounts).toMutableList()

        return folder.copy(
            feeds = folderFeeds,
            unreadCount = folderFeeds.sumOf { it.unreadCount }
        )
    }

    private fun copyFeedUnreadCounts(feed: Feed): Feed {
        return feed.copy(unreadCount = _unreadCounts.value.getOrDefault(feed.id, 0))
    }

    private fun refreshUnreadCounts() {
        viewModelScope.launch {
            _unreadCounts.value = accountState.value.unreadCounts
        }
    }
}

private fun ArticleFilter.Companion.findOrDefault(preferences: Preferences): ArticleFilter {
    return preferences.filter ?: default()
}

private fun Account.findArticleOrNull(preferences: Preferences): Article? {
    return preferences.articleID?.let { findArticle(it) }
}
