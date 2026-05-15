package com.capyreader.app.ui.articles.feeds.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Folder
import com.jocmp.capy.common.sortedByTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFeedViewModel(
    private val account: Account,
) : ViewModel() {
    val folders: Flow<List<Folder>> = account.folders.map { it.sortedByTitle() }
    val showMultiselect = account.supportsMultiFolderFeeds

    fun submit(form: EditFeedFormEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                account.editFeed(form = form)
            }
        }
    }
}
