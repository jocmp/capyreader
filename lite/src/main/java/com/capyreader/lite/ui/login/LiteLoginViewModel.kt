package com.capyreader.lite.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.lite.preferences.LitePreferences
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiteLoginState(
    val source: Source = Source.LOCAL,
    val username: String = "",
    val password: String = "",
    val serverURL: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
)

class LiteLoginViewModel(
    private val accountManager: AccountManager,
    private val litePreferences: LitePreferences,
) : ViewModel() {
    private val _state = MutableStateFlow(LiteLoginState())
    val state: StateFlow<LiteLoginState> = _state.asStateFlow()

    fun setSource(source: Source) = _state.update { it.copy(source = source) }
    fun setUsername(value: String) = _state.update { it.copy(username = value) }
    fun setPassword(value: String) = _state.update { it.copy(password = value) }
    fun setServerURL(value: String) = _state.update { it.copy(serverURL = value) }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val accountID = if (s.source == Source.LOCAL) {
                accountManager.createAccount(source = Source.LOCAL)
            } else {
                accountManager.createAccount(
                    username = s.username,
                    password = s.password,
                    url = s.serverURL,
                    source = s.source,
                )
            }
            litePreferences.accountID.set(accountID)
            _state.update { it.copy(submitting = false) }
            onSuccess()
        }
    }
}
