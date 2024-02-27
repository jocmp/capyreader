package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.EditFeedForm
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.preferences.getAndSet
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EditFeedViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager,
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val account = accountManager.findByID(appPreferences.accountID.get())!!
    private val args = EditFeedArgs(savedStateHandle)

    val feed: Feed
        get() = runBlocking { account.findFeed(args.feedID)!! }

    val feedFolderTitles: List<String>
        get() = emptyList()  // account.folders.filter { it.feeds.contains(feed) }.map { it.title }

    val folders: List<Folder>
        get() = emptyList() // account.folders.toList()

    fun submit(form: EditFeedForm, onSuccess: () -> Unit) {
        viewModelScope.launch {
            account
                .editFeed(form = form)
                .onSuccess { feed ->
                    appPreferences.filter.getAndSet { filter ->
                        if (filter.isFeedSelected(feed)) {
                            ArticleFilter.Feeds(feed, filter.status)
                        } else {
                            filter
                        }
                    }
                    onSuccess()
                }
        }
    }
}
