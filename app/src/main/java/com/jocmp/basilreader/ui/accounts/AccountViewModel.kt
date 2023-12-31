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
import com.jocmp.basil.Feed
import com.jocmp.basil.FeedFormEntry
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.Folder
import com.jocmp.basil.buildPager
import com.jocmp.basilreader.selectAccount
import com.jocmp.basilreader.selectedAccount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AccountViewModel(
    private val accountManager: AccountManager,
    private val settings: DataStore<Preferences>,
) : ViewModel() {
    private val accountState: MutableState<Account?> = mutableStateOf(
        null,
        policy = neverEqualPolicy()
    )

    private val filter = mutableStateOf<ArticleFilter>(ArticleFilter.default())

    private val pager: MutableState<Pager<Int, Article>?> = mutableStateOf(null)

    init {
        viewModelScope.launch {
            val accountID = settings.data.first().selectedAccount()

            if (accountID != null) {
                selectAccount(accountID)
            } else {
                val firstID = accountManager.accountIDs().firstOrNull()
                firstID?.let {
                    selectSettingsAccount(firstID)
                }
            }

            pager.value = account?.buildPager(filter.value)
        }
    }

    private val account: Account?
        get() = accountState.value

    val folders: List<Folder>
        get() = account?.folders?.toList() ?: emptyList()

    private val articleState = mutableStateOf<Article?>(null)

    val feeds: List<Feed>
        get() = account?.feeds?.toList() ?: emptyList()

    val article: Article?
        get() = articleState.value

    fun articles(): Pager<Int, Article>? = pager.value

    fun selectFeed(feedID: String, onComplete: () -> Unit) {
        val feed = account?.findFeed(feedID) ?: return
        val feedFilter = ArticleFilter.Feeds(feed = feed, status = filter.value.status)

        selectFilter(feedFilter, onComplete)
    }

    fun selectFolder(title: String, onComplete: () -> Unit) {
        val folder = account?.findFolder(title) ?: return
        val feedFilter = ArticleFilter.Folders(folder = folder, status = filter.value.status)

        selectFilter(feedFilter, onComplete)
    }

    suspend fun refreshFeed() {
        when (val currentFilter = filter.value) {
            is ArticleFilter.Feeds -> account?.refreshFeed(currentFilter.feed)
            is ArticleFilter.Folders ->  account?.refreshFeeds(currentFilter.folder.feeds)
            is ArticleFilter.Articles -> account?.refreshAll()
        }
    }

    fun selectArticle(articleID: String) {
        articleState.value = account?.findArticle(articleID.toLong())
    }

    fun clearArticle() {
        articleState.value = null
    }

    private fun selectFilter(nextFilter: ArticleFilter, onComplete: () -> Unit) {

        filter.value = nextFilter
        pager.value = account?.buildPager(filter = nextFilter)

        clearArticle()

        onComplete()
    }

    private fun selectSettingsAccount(accountID: String) {
        viewModelScope.launch {
            settings.selectAccount(accountID)
        }
    }

    private fun selectAccount(accountID: String) {
        accountState.value = accountManager.findByID(accountID)
    }

    fun addFeed(entry: FeedFormEntry, onSuccess: () -> Unit) {
        viewModelScope.launch {
            account?.addFeed(entry)?.onSuccess {
                accountState.value = account
                onSuccess()
            }
        }
    }
}
