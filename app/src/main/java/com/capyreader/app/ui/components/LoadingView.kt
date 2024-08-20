package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun LoadingView(
    progressAlignment: Alignment = Alignment.Center,
    progressIndicatorDelay: Long = 1200L,
) {
    val (showLoading, setLoading) = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = progressAlignment
    ) {
        if (showLoading) {
            CircularProgressIndicator()
        }
    }

    LaunchedEffect(Unit) {
        delay(progressIndicatorDelay)
        setLoading(true)
    }
}
