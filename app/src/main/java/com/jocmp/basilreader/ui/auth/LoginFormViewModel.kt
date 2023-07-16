package com.jocmp.basilreader.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.jocmp.feedbinclient.Account
import com.jocmp.feedbinclient.Authentication
import com.jocmp.feedbinclient.CredentialsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginFormViewModel(
    val emailAddress: String = "",
    val password: String = "",
    val setEmailAddress: (String) -> Unit,
    val setPassword: (String) -> Unit,
    val login: () -> Unit,
    val isAuthenticated: Boolean,
)

@Composable
fun useLoginFormViewModel(credentialsManager: CredentialsManager, authentication: Authentication): LoginFormViewModel {
    val coroutineScope = rememberCoroutineScope()
    val (emailAddress, setEmailAddress) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }
    val isAuthenticated = credentialsManager.hasAccount

    fun loginUser() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                if (authentication.login(username = emailAddress, password = password)) {
                    val account = Account(username = emailAddress, password = password)
                    credentialsManager.save(account = account)
                }
            }
        }
    }

    return LoginFormViewModel(
        emailAddress = emailAddress,
        password = password,
        setPassword = setPassword,
        setEmailAddress = setEmailAddress,
        login = { loginUser() },
        isAuthenticated = isAuthenticated
    )
}
