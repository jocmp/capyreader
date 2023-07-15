package com.jocmp.basilreader.ui.auth

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.jocmp.basilreader.R

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
