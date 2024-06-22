package com.jocmp.capyreader.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.verifyCredentials
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.loadAccountModules
import kotlinx.coroutines.launch

class LoginViewModel(
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    fun login(
        username: String,
        password: String,
        onComplete: (result: Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = verifyCredentials(username = username, password = password)

            if (result) {
                val accountID = accountManager.createAccount(
                    username = username,
                    password = password,
                )

                selectAccount(accountID)

                loadAccountModules()

                Result.success(Unit)
            } else {
                Result.failure(Exception("Couldn't log in"))
            }
        }
    }

    private fun selectAccount(id: String) {
        appPreferences.articleID.delete()
        appPreferences.filter.delete()
        appPreferences.accountID.set(id)
    }
}
