package com.capyreader.app.ui.articles

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CountBadge(count: Long) {
    if (count < 1) {
        return
    }

    Text(count.toString())
}
