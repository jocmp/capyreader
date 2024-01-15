package com.jocmp.basilreader.ui.articles

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.selectedAccountID

class EditFolderViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager,
    settings: DataStore<Preferences>,
) : ViewModel() {
    private val account = accountManager.findByID(settings.selectedAccountID)!!
    private val args = EditFolderArgs(savedStateHandle)
}
