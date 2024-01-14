package com.jocmp.basilreader.ui.articles

import EditFeedForm
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.selectedAccountID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EditFeedViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager,
    private val settings: DataStore<Preferences>,
) : ViewModel() {
    private val initialPreferences = runBlocking { settings.data.first() }
    private val account = accountManager.findByID(initialPreferences.selectedAccountID)!!
    private val args = EditFeedArgs(savedStateHandle)

    val feed: Feed?
        get() = account.findFeed(args.feedID)

    val feedFolderTitles: List<String>
        get() = account.folders.filter { it.feeds.contains(feed) }.map { it.title }

    val folders: List<Folder>
        get() = account.folders.toList()

    fun submit(form: EditFeedForm, onSuccess: () -> Unit) {
        viewModelScope.launch {
            account
                .editFeed(form = form)
                .onSuccess {
                    onSuccess()
                }
        }
    }
}
