package com.jocmp.capyreader.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinInject(),
    onSuccess: () -> Unit,
) {
    val (username, setUsername) = rememberSaveable {
        mutableStateOf("")
    }

    val (password, setPassword) = rememberSaveable {
        mutableStateOf("")
    }

    val login = {
        viewModel.login(username, password) { result ->
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = {}
            )
        }
    }

    Scaffold { padding ->
        Column(Modifier.padding(padding)) {
            AuthFields(
                username = username,
                password = password,
                onUsernameChange = setUsername,
                onPasswordChange = setPassword,
                onSubmit = login,
            )
        }
    }

    LaunchedEffect(Unit) {
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
    }
}


@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        onSuccess = {}
    )
}
