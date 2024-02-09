package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.EditFolderForm
import com.jocmp.basil.Folder
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.launch

class EditFolderViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager,
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val account = accountManager.findByID(appPreferences.accountID.get())!!
    private val args = EditFolderArgs(savedStateHandle)

    val folder: Folder
        get() = account.findFolder(args.folderTitle)!!

    fun submit(form: EditFolderForm, onSubmit: () -> Unit) {
        viewModelScope.launch {
            account.editFolder(form = form).onSuccess { updatedFolder ->
                val folderFilter = appPreferences.filter.get() as? ArticleFilter.Folders

                folderFilter?.let { filter ->
                    if (filter.folder.title == form.existingTitle) {
                        appPreferences.filter.set(filter.copy(folder = updatedFolder))
                    }
                }

                onSubmit()
            }
        }
    }
}
