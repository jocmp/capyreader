package com.jocmp.basilreader.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.AppPreferences
import kotlinx.coroutines.flow.Flow

class AccountSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AccountManager
): ViewModel() {
    private val args = AccountSettingsArgs(savedStateHandle)

    val account = accountManager.findByID(args.accountID)!!

    fun submitName(displayName: String) {
        account.displayName.set(displayName)
    }
}
