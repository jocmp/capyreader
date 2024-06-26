package com.jocmp.capyreader.ui.accounts

import androidx.lifecycle.ViewModel
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.loadAccountModules

class AddAccountViewModel(
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    fun addLocalAccount() {
        val accountID = accountManager.createAccount(source = Source.LOCAL)

        selectAccount(accountID)

        loadAccountModules()
    }

    private fun selectAccount(id: String) {
        appPreferences.accountID.set(id)
    }
}
