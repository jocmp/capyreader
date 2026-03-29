package com.capyreader.app.ui.components

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.launch

@Composable
fun buildCopyToClipboard(text: String): () -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    return {
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("", text)))
        }
    }
}
