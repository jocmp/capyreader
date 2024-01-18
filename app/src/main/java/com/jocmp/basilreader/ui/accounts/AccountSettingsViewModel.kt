package com.jocmp.basilreader.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.AppPreferences

class AccountSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager,
    private val appPreferences: AppPreferences,
): ViewModel() {
}
