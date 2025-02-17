package com.capyreader.app.ui.articles

import androidx.lifecycle.ViewModel
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Folder
import com.jocmp.capy.preferences.getAndSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EditFeedViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val folders: Flow<List<Folder>> = account.folders
    val showMultiselect = account.supportsMultiFolderFeeds

    suspend fun submit(
        form: EditFeedFormEntry,
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            account
                .editFeed(form = form)
                .fold(
                    onSuccess = { feed ->
                        appPreferences.filter.getAndSet { filter ->
                            if (filter.isFeedSelected(feed)) {
                                ArticleFilter.Feeds(
                                    feedID = feed.id,
                                    folderTitle = null,
                                    filter.status
                                )
                            } else {
                                filter
                            }
                        }

                        Result.success(Unit)
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
        }
    }
}
