package com.capyreader.app.ui.accounts

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.common.Async
import com.jocmp.capy.common.launchIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateLoginViewModel(
    private val account: Account,
    private val clientCertManager: ClientCertManager,
) : ViewModel() {
    var username by mutableStateOf("")
        private set
    val source = account.source
    private var url by mutableStateOf("")

    init {
        viewModelScope.launch {
            username = account.preferences.username.get()
            url = account.preferences.url.get()
        }
    }

    private var _password by mutableStateOf("")
    private var _clientCertAlias by mutableStateOf("")
    private var _result by mutableStateOf<Async<Unit>>(Async.Uninitialized)

    val password: String
        get() = _password

    val clientCertAlias: String
        get() = _clientCertAlias

    val loading: Boolean
        get() = _result is Async.Loading

    val errorMessage: String?
        get() = (_result as? Async.Failure)?.error?.message

    fun setPassword(password: String) {
        _password = password
    }

    fun chooseClientCert(activity: Activity) {
        clientCertManager.chooseClientCert(activity) { alias ->
            _clientCertAlias = alias
        }
    }

    fun clearClientCert() {
        _clientCertAlias = ""
    }

    fun submit(onSuccess: () -> Unit) {
        if (password.isBlank()) {
            _result = Async.Failure(loginError())
        }

        viewModelScope.launchIO {
            _result = Async.Loading

            credentials.verify()
                .onSuccess { result ->
                    updateAccount(result)

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
                .onFailure {
                    _result = Async.Failure(it)
                }
        }
    }

    private val credentials: Credentials
        get() = Credentials.from(
            source = source,
            username = username,
            password = password,
            url = url,
            clientCertAlias = clientCertAlias,
            clientCertManager = clientCertManager,
        )

    private suspend fun updateAccount(result: Credentials) {
        account.preferences.password.set(result.secret)
    }

    private fun loginError() = Error("Error logging in")
}
