package com.capyreader.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.jocmp.capy.preferences.Preference

@Composable
fun <T> Preference<T>.asState(): State<T?> {
    return this
        .changes()
        .collectAsState(initial = null)
}
