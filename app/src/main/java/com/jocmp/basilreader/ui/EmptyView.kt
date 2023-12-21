package com.jocmp.basilreader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EmptyView(fullWidth: Boolean = false) {
    val modifier = if (fullWidth) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
    }

    Column(modifier) {}
}
