package com.jocmp.basilreader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EmptyView(fillSize: Boolean = false) {
    val modifier = if (fillSize) {
        Modifier.fillMaxSize()
    } else {
        Modifier
    }

    Column(modifier) {}
}
