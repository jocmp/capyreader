package com.jocmp.capyreader.ui.articles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.accounts.verifyCredentials
import com.jocmp.capyreader.common.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateAuthViewModel(
    private val account: Account
) : ViewModel() {
    private var _password by mutableStateOf("")
    private var _result by mutableStateOf<Async<Unit>>(Async.Uninitialized)

    val username = account.preferences.username.get()

    val password: String
        get() = _password

    val loading: Boolean
        get() = _result is Async.Loading

    val showError: Boolean
        get() = _result is Async.Failure

    fun setPassword(password: String) {
        _password = password
    }

    fun submit(onSuccess: () -> Unit) {
        if (password.isBlank()) {
            _result = Async.Failure(loginError())
        }

        viewModelScope.launch(Dispatchers.IO) {
            _result = Async.Loading

            val isSuccessful = verifyCredentials(username = username, password = password)

            if (isSuccessful) {
                account.preferences.password.set(password)
                onSuccess()
            } else {
                _result = Async.Failure(loginError())
            }
        }
    }

    private fun loginError() = Error("Error logging in")
}
