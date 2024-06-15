package com.jocmp.capyreader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EmptyView(
    fillSize: Boolean = false,
    showLoading: Boolean = false,
) {
    val modifier = if (fillSize) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier
            )
        }
    }
}

@Preview
@Composable
private fun EmptyViewPreview() {
    EmptyView(
        showLoading = true
    )
}
