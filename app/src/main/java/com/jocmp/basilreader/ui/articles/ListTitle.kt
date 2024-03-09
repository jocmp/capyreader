package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ListTitle(text: String) {
    Text(
        text,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}
