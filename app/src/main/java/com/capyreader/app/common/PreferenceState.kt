package com.capyreader.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.jocmp.capy.preferences.Preference

@Composable
fun <T> Preference<T>.asState(): State<T> {
    val scope = rememberCoroutineScope()

    return this
        .stateIn(scope)
        .collectAsState()
}
