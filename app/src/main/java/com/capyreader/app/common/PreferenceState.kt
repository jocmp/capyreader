package com.capyreader.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.jocmp.capy.preferences.Preference

@Composable
fun <T> Preference<T>.asState(): State<T> {
    val key = key()
    val changes = remember(key) { changes() }
    val initial = remember(key) { get() }

    return changes.collectAsState(initial = initial)
}
