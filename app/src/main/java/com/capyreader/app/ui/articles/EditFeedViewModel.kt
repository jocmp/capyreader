package com.capyreader.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Folder
import com.jocmp.capy.preferences.getAndSet
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.refresher.RefreshInterval
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
