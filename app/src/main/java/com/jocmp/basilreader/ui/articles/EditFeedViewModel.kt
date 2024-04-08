package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.Account
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.EditFeedForm
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.preferences.getAndSet
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFeedViewModel(
    savedStateHandle: SavedStateHandle,
    private val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val args = EditFeedArgs(savedStateHandle)

    private val _feed = mutableStateOf<Feed?>(null)

    private val _folders = mutableStateOf<List<Folder>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val feed = account.findFeed(args.feedID)
            val folders = account.folders.first()

            withContext(Dispatchers.Main) {
                _feed.value = feed
                _folders.value = folders
            }
        }
    }

    val feed: Feed?
        get() = _feed.value

    val folders: List<Folder>
        get() = _folders.value

    val feedFolderTitles: List<String>
        get() = folders
            .filter { folder -> folder.feeds.any { it.id == feed?.id } }
            .map { it.title }

    fun submit(form: EditFeedForm, onSuccess: () -> Unit) {
        viewModelScope.launch {
            account
                .editFeed(form = form)
                .onSuccess { feed ->
                    appPreferences.filter.getAndSet { filter ->
                        if (filter.isFeedSelected(feed)) {
                            ArticleFilter.Feeds(feed.id, filter.status)
                        } else {
                            filter
                        }
                    }
                    onSuccess()
                }
        }
    }
}
