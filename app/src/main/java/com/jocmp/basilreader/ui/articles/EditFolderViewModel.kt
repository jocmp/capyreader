package com.jocmp.basilreader.ui.articles

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.EditFolderForm
import com.jocmp.basil.Folder
import com.jocmp.basilreader.filter
import com.jocmp.basilreader.putFilter
import com.jocmp.basilreader.selectedAccountID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditFolderViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager,
    private val settings: DataStore<Preferences>,
) : ViewModel() {
    private val account = accountManager.findByID(settings.selectedAccountID)!!
    private val args = EditFolderArgs(savedStateHandle)

    val folder: Folder
        get() = account.findFolder(args.folderTitle)!!

    fun submit(form: EditFolderForm, onSubmit: () -> Unit) {
        viewModelScope.launch {
            account.editFolder(form = form).onSuccess { updatedFolder ->
                val folderFilter = settings.data.first().filter as? ArticleFilter.Folders

                folderFilter?.let { filter ->
                    if (filter.folder.title == form.existingTitle) {
                        settings.putFilter(articleFilter = filter.copy(folder = updatedFolder))
                    }
                }

                onSubmit()
            }
        }
    }
}
