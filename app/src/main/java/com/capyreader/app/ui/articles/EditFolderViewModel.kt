package com.capyreader.app.ui.articles

import androidx.lifecycle.ViewModel
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.EditFolderFormEntry
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.preferences.getAndSet

class EditFolderViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {

    suspend fun submit(
        form: EditFolderFormEntry,
    ): Result<Unit> {
        return withIOContext {
            account
                .editFolder(form = form)
                .fold(
                    onSuccess = { folder ->
                        appPreferences.filter.getAndSet { filter ->
                            if (filter is ArticleFilter.Folders) {
                                ArticleFilter.Folders(
                                    folderTitle = folder.title,
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
