package com.jocmp.basilreader.ui.accounts

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jocmp.basil.AccountManager

class AccountSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager,
    private val settings: DataStore<Preferences>,
): ViewModel() {
}
