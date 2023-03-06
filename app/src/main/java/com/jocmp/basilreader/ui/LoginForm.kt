package com.jocmp.basilreader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

// https://issuetracker.google.com/issues/176949051
@Composable
fun LoginForm() {
    val viewModel = useLoginFormViewModel()

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
