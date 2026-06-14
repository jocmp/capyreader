package com.capyreader.app.ui.accounts

import androidx.lifecycle.ViewModel
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.loadAccountModules
import com.capyreader.app.refresher.RefreshScheduler

class AddAccountViewModel(
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
    private val refreshScheduler: RefreshScheduler,
) : ViewModel() {
    fun addLocalAccount() {
        val accountID = accountManager.createAccount(source = Source.LOCAL)

        selectAccount(accountID)

        loadAccountModules()

        refreshScheduler.initialize()
    }

    private fun selectAccount(id: String) {
        appPreferences.accountID.set(id)
    }
}
