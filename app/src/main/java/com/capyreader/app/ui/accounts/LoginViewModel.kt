package com.capyreader.app.ui.accounts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.loadAccountModules
import com.capyreader.app.ui.Route
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.withFreshRSSPath
import com.jocmp.capy.common.Async
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.withTrailingSeparator
import com.jocmp.feedfinder.withProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel(
    handle: SavedStateHandle,
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private var _username by mutableStateOf("")
    private var _password by mutableStateOf("")
    private var _url by mutableStateOf("")
    private var _result by mutableStateOf<Async<Unit>>(Async.Uninitialized)
    val source = handle.toRoute<Route.Login>().source

    val username
        get() = _username

    val password
        get() = _password

    val url
        get() = _url

    val loading: Boolean
        get() = _result is Async.Loading

    val errorMessage: String?
        get() = (_result as? Async.Failure)?.error?.message

    fun setUsername(username: String) {
        _username = username
    }

    fun setPassword(password: String) {
        _password = password
    }

    fun setURL(url: String) {
        _url = url
    }

    fun submit(onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _result = Async.Failure(loginError())
        }

        normalizeURL()

        viewModelScope.launchIO {
            _result = Async.Loading

            credentials.verify()
                .onSuccess { result ->
                    createAccount(result)

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
                .onFailure {
                    _result = Async.Failure(it)
                }
        }
    }

    private fun normalizeURL() {
        if (!source.hasCustomURL || _url.isBlank()) {
           return
        }

        _url = _url
            .withProtocol
            .withTrailingSeparator
            .let { withFreshRSSPath(it, source) }
    }

    private val credentials: Credentials
        get() = Credentials.from(
            source = source,
            username = username,
            password = password,
            url = _url
        )

    private fun createAccount(credentials: Credentials) {
        val accountID = accountManager.createAccount(
            username = credentials.username,
            password = credentials.secret,
            url = credentials.url,
            source = credentials.source
        )

        selectAccount(accountID)

        loadAccountModules()
    }

    private fun selectAccount(id: String) {
        appPreferences.accountID.set(id)
    }

    private fun loginError() = Error("Error logging in")
}
