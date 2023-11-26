package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import kotlinx.coroutines.launch
import java.util.UUID

class AccountViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager
) : ViewModel() {
    private val args = ArticleArgs(savedStateHandle)

    private val accountState = mutableStateOf(accountManager.findByID(args.accountId))
    private val account: Account
        get() = accountState.value

    val feeds = account.feeds.toList()
    val folders = account.folders.toList()

    fun addFeed(url: String) {
        viewModelScope.launch {
            val nextAccount = accountState.value.copy()
            nextAccount.addFeed(url = url)
            accountState.value = nextAccount
        }
    }
}
