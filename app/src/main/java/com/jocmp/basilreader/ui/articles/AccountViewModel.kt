package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.FeedFormEntry
import kotlinx.coroutines.launch

class AccountViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager
) : ViewModel() {
    private val args = ArticleArgs(savedStateHandle)

    private val accountState = mutableStateOf(
        accountManager.findByID(args.accountID),
        policy = neverEqualPolicy()
    )

    val account: Account
        get() = accountState.value

    val feeds = account.feeds.toList()
    val folders = account.folders.toList()

    fun addFeed(entry: FeedFormEntry, onSuccess: () -> Unit) {
        viewModelScope.launch {
            account.addFeed(entry)
            accountState.value = account
            onSuccess()
        }
    }
}
