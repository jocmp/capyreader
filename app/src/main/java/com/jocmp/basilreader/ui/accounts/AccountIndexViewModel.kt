package com.jocmp.basilreader.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basil.accounts.verifyCredentials
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.launch

class AccountIndexViewModel(
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        viewModelScope.launch {
            val result = verifyCredentials(username = username, password = password)

            if (result) {
                val accountID = accountManager.createAccount(
                    username = username,
                    password = password,
                )

                selectAccount(accountID)

                onSuccess()
            } else {
                onFailure()
            }
        }
    }

    private fun selectAccount(id: String) {
        appPreferences.articleID.delete()
        appPreferences.filter.delete()
        appPreferences.accountID.set(id)
    }
}
