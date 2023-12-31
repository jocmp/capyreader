package com.jocmp.basilreader.ui.accounts

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Feed
import com.jocmp.basil.FeedFormEntry
import com.jocmp.basil.Folder
import com.jocmp.basil.buildPager
import com.jocmp.basilreader.selectAccount
import com.jocmp.basilreader.selectedAccount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AccountViewModel(
    private val accountManager: AccountManager,
    private val settings: DataStore<Preferences>,
) : ViewModel() {
    private val accountState: MutableState<Account> = mutableStateOf(
        accountManager.findByID(runBlocking { settings.data.first() }.selectedAccount())!!,
        policy = neverEqualPolicy()
    )

    private val filter = mutableStateOf<ArticleFilter>(ArticleFilter.default())

    private val _pager = mutableStateOf(account.buildPager(filter.value))

    val pager: Pager<Int, Article>
        get() = _pager.value

    private val account: Account
        get() = accountState.value

    val folders: List<Folder>
        get() = account.folders.toList()

    private val articleState = mutableStateOf<Article?>(null)

    val feeds: List<Feed>
        get() = account.feeds.toList()

    val article: Article?
        get() = articleState.value

    val filterStatus: ArticleStatus
        get() = filter.value.status

    fun selectStatus(status: ArticleStatus) {
        val nextFilter = filter.value.withStatus(status = status)
        filter.value = nextFilter

        _pager.value = account.buildPager(nextFilter)
    }

    fun selectFeed(feedID: String, onComplete: () -> Unit) {
        val feed = account.findFeed(feedID) ?: return
        val feedFilter = ArticleFilter.Feeds(feed = feed, status = filter.value.status)

        selectFilter(feedFilter, onComplete)
    }

    fun selectFolder(title: String, onComplete: () -> Unit) {
        val folder = account.findFolder(title) ?: return
        val feedFilter = ArticleFilter.Folders(folder = folder, status = filter.value.status)

        selectFilter(feedFilter, onComplete)
    }

    suspend fun refreshFeed() {
        when (val currentFilter = filter.value) {
            is ArticleFilter.Feeds -> account.refreshFeed(currentFilter.feed)
            is ArticleFilter.Folders -> account.refreshFeeds(currentFilter.folder.feeds)
            is ArticleFilter.Articles -> account.refreshAll()
        }
    }

    fun selectArticle(articleID: String) {
        articleState.value = account.findArticle(articleID.toLong())
    }

    fun clearArticle() {
        articleState.value = null
    }

    private fun selectFilter(nextFilter: ArticleFilter, onComplete: () -> Unit) {
        filter.value = nextFilter
        _pager.value = account.buildPager(nextFilter)

        clearArticle()

        onComplete()
    }

    private fun selectSettingsAccount(accountID: String) {
        viewModelScope.launch {
            settings.selectAccount(accountID)
        }
    }

    private fun selectAccount(accountID: String) {
        accountManager.findByID(accountID)?.let {
            accountState.value = it
        }
    }

    fun addFeed(entry: FeedFormEntry, onSuccess: () -> Unit) {
        viewModelScope.launch {
            account.addFeed(entry).onSuccess {
                accountState.value = account
                onSuccess()
            }
        }
    }
}
