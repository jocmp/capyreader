package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.Account
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.EditFeedFormEntry
import com.jocmp.basil.Folder
import com.jocmp.basil.preferences.getAndSet
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFeedViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val folders: Flow<List<Folder>> = account.folders

    fun submit(
        form: EditFeedFormEntry,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
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

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
                .onFailure { onFailure() }
        }
    }
}
