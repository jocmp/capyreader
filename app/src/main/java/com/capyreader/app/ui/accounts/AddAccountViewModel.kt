package com.capyreader.app.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.loadAccountModules
import kotlinx.coroutines.launch

class AddAccountViewModel(
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    fun addLocalAccount() {
        viewModelScope.launch {
            val accountID = accountManager.createAccount(source = Source.LOCAL)

            selectAccount(accountID)

            loadAccountModules(accountID)
        }
    }

    private suspend fun selectAccount(id: String) {
        appPreferences.accountID.set(id)
    }
}
