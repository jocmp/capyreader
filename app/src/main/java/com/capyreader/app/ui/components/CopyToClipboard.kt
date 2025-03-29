package com.capyreader.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun buildCopyToClipboard(text: String): () -> Unit {
    val clipboardManager = LocalClipboardManager.current

    return {
        clipboardManager.setText(AnnotatedString(text))
    }
}
