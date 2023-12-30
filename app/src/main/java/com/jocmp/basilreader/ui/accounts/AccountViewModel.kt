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
import com.jocmp.basil.Filter
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

            pager.value = account?.buildPager()
        }
    }

    private val account: Account?
        get() = accountState.value

    val folders: List<Folder>
        get() = account?.folders?.toList() ?: emptyList()

    private val feedID = mutableStateOf<String?>(null)

    private val articleState = mutableStateOf<Article?>(null)

    val feeds: List<Feed>
        get() = account?.feeds?.toList() ?: emptyList()

    val article: Article?
        get() = articleState.value

    fun articles(): Pager<Int, Article>? = pager.value

    fun selectFeed(feedID: String, onComplete: () -> Unit) {
        val feed = account?.findFeed(feedID)

        this.feedID.value = feed?.id

        if (feed != null) {
            pager.value = account?.buildPager(
                filter = Filter.Feeds(feed = feed, status = Filter.Status.ALL),
            )
        }

        clearArticle()

        onComplete()
    }

    suspend fun refreshFeed() {
        val id = feedID.value ?: return

        account?.refreshFeed(id)
    }

    fun selectArticle(articleID: String) {
        articleState.value = account?.findArticle(articleID.toLong())
    }

    fun clearArticle() {
        articleState.value = null
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
            account?.addFeed(entry)
            accountState.value = account
            onSuccess()
        }
    }
}
