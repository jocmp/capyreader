package com.jocmp.basilreader.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.jocmp.basilreader.R
import com.jocmp.basilreader.Route
import com.jocmp.basilreader.ui.get
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

// https://issuetracker.google.com/issues/176949051
@Composable
fun LoginForm(onSuccess: () -> Unit = {}) {
    val viewModel = useLoginForm(get(), get(), onSuccess)

    Column {
        AutofillUsernameField(
            value = viewModel.emailAddress,
            onValueChange = viewModel.setEmailAddress
        )
        AutofillPasswordField(
            value = viewModel.password,
            onValueChange = viewModel.setPassword
        )
        Button(onClick = { viewModel.login() }) {
            Text(stringResource(R.string.login_form_button_text))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BasilReaderTheme {
        LoginForm()
    }
}
