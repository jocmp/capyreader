package com.jocmp.basilreader.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.jocmp.basil.accounts.Account
import com.jocmp.basil.accounts.CredentialsManager
import com.jocmp.basil.accounts.CredentialsManager.Companion.accountID
import com.jocmp.feedbin.FeedbinClient
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
fun useLoginFormViewModel(): LoginFormViewModel {
    val feedbinClient = FeedbinClient()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val (emailAddress, setEmailAddress) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }
    val (isAuthenticated, setAuthenticated) = remember {
        mutableStateOf(CredentialsManager.fetchAccount(context) != null)
    }

    fun loginUser() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val isSuccess = feedbinClient.authentication(emailAddress, password)

                if (isSuccess) {
                    val account =
                        Account(id = accountID, username = emailAddress, password = password)
                    CredentialsManager.saveAccount(account = account, context)


                    withContext(Dispatchers.Main) {
                        setAuthenticated(true)
                    }
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
