package com.capyreader.app.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.loadAccountModules
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.common.launchIO

class AddAccountViewModel(
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    fun addLocalAccount() {
        viewModelScope.launchIO {
            val accountID = accountManager.createAccount(source = Source.LOCAL)

            selectAccount(accountID)

            loadAccountModules()
        }
    }

    private fun selectAccount(id: String) {
        viewModelScope.launchIO {
            appPreferences.accountID.set(id)
        }
    }
}
