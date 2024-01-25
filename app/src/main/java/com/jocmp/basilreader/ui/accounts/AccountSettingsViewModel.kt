package com.jocmp.basilreader.ui.accounts

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.URI

class AccountSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AccountManager
): ViewModel() {
    private val args = AccountSettingsArgs(savedStateHandle)

   private val _account = mutableStateOf(
       accountManager.findByID(args.accountID)!!,
       policy = neverEqualPolicy()
   )

    val account: Account
        get() = _account.value

    val displayName: String
        get() = account.displayName

    fun submitName(displayName: String) {
        account.displayName = displayName
        _account.value = account
    }

    fun removeAccount() {
        accountManager.removeAccount(accountID = account.id)
    }

    fun importOPML(inputStream: InputStream?, onSuccess: () -> Unit = {}) {
        inputStream ?: return

        viewModelScope.launch {
            account.import(inputStream)
            onSuccess()
        }
    }
}
