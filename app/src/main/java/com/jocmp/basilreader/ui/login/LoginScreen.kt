package com.jocmp.basilreader.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType.EmailAddress
import androidx.compose.ui.autofill.AutofillType.Password
import com.jocmp.basilreader.ui.autofill
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    viewModel: AccountIndexViewModel = koinInject(),
    onSuccess: () -> Unit,
) {
    val (username, setUsername) = rememberSaveable {
        mutableStateOf("")
    }

    val (password, setPassword) = rememberSaveable {
        mutableStateOf("")
    }

    val login = {
        viewModel.login(username, password,
            onSuccess = onSuccess,
            onFailure = { /* Show toast */ }
        )
    }

    Column {
        TextField(
            value = username,
            onValueChange = setUsername,
            label = {
                Text("Username")
            },
            modifier = Modifier.autofill(
                listOf(EmailAddress),
                onFill = setUsername
            )
        )
        TextField(
            value = password,
            onValueChange = setPassword,
            label = {
                Text("Password")
            },
            modifier = Modifier.autofill(
                listOf(Password),
                onFill = setPassword
            )
        )
        Button(onClick = { login() }) {
            Text("Save")
        }
    }
}
