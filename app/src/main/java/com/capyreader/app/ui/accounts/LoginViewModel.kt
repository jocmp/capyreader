package com.capyreader.app.ui.accounts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.verifyCredentials
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.common.Async
import com.capyreader.app.loadAccountModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(
    private val account: Account? = null,
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private var _username by mutableStateOf(account?.preferences?.username?.get().orEmpty())
    private var _password by mutableStateOf("")
    private var _result by mutableStateOf<Async<Unit>>(Async.Uninitialized)

    val username: String
        get() = _username

    val password: String
        get() = _password

    val loading: Boolean
        get() = _result is Async.Loading

    val showError: Boolean
        get() = _result is Async.Failure

    fun setUsername(username: String) {
        _username = username
    }

    fun setPassword(password: String) {
        _password = password
    }

    fun submit(onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _result = Async.Failure(loginError())
        }

        viewModelScope.launch(Dispatchers.IO) {
            _result = Async.Loading

            val isSuccessful = verifyCredentials(username = username, password = password)

            if (isSuccessful) {
                withContext(Dispatchers.Main) {
                    updateOrCreateAccount()
                    onSuccess()
                }
            } else {
                _result = Async.Failure(loginError())
            }
        }
    }

    private fun updateOrCreateAccount() {
        if (account == null) {
            val accountID = accountManager.createAccount(
                username = username,
                password = password,
                source = Source.FEEDBIN
            )

            selectAccount(accountID)

            loadAccountModules()
        } else {
            account.preferences.password.set(password)
        }
    }

    private fun selectAccount(id: String) {
        appPreferences.accountID.set(id)
    }

    private fun loginError() = Error("Error logging in")
}
