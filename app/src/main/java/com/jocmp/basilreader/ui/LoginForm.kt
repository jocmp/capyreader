package com.jocmp.basilreader.ui

import android.annotation.SuppressLint
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IntegerRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.theme.BasilReaderTheme
import com.jocmp.feedbin.FeedbinClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Login() {
    val feedbinClient = FeedbinClient()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val (emailAddress, setEmailAddress) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }

    fun loginUser() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val isSuccess = feedbinClient.authentication(emailAddress, password)

                if (isSuccess) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Good stuff", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column {
        AutofillUsernameField(
            value = emailAddress,
            onValueChange = setEmailAddress
        )
        AutofillPasswordField(
            value = password,
            onValueChange = setPassword
        )
        Button(onClick = { loginUser() }) {
            Text("Log in")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BasilReaderTheme {
        Login()
    }
}

@SuppressLint("InflateParams")
@Composable
fun AutofillPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth(),
        factory = { context ->
            val layout = LayoutInflater.from(context).inflate(R.layout.autofill_password_field, null)
            layout.findViewById<TextInputEditText>(R.id.autofill_password_field_edit_text).apply {
                setText(value)
                imeOptions = EditorInfo.IME_ACTION_DONE
                doAfterTextChanged {
                    onValueChange(safeText)
                }
            }
            layout
        }
    )
}

@SuppressLint("InflateParams")
@Composable
fun AutofillUsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,

) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth(),
        factory = { context ->
            val layout = LayoutInflater.from(context).inflate(R.layout.autofill_username_field, null)
            layout.findViewById<TextInputEditText>(R.id.autofill_username_field_edit_text).apply {
                setText(value)
                imeOptions = EditorInfo.IME_ACTION_NEXT
                doAfterTextChanged {
                    onValueChange(safeText)
                }
            }
            layout
        }
    )
}

val EditText?.safeText: String get() =
    this?.editableText?.toString().orEmpty().trim()
