package com.jocmp.basilreader.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.AppPreferences
import kotlinx.coroutines.flow.Flow

class AccountSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AccountManager
): ViewModel() {
    private val args = AccountSettingsArgs(savedStateHandle)

    private val account = accountManager.findByID(args.accountID)!!

    val displayName: String
        get() = account.displayName

    fun submitName(displayName: String) {
        account.displayName = displayName
    }

    fun removeAccount() {
        accountManager.removeAccount(accountID = account.id)
    }
}
