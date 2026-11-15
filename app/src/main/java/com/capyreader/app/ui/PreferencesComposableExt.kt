package com.capyreader.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.capyreader.app.common.asState
import com.jocmp.capy.preferences.Preference

@Composable
fun <T> Preference<T>.collectChangesWithDefault(initial: T = defaultValue()): State<T> {
    val changes = remember(key()) { changes() }

    return changes.collectAsState(initial = initial)
}

@Composable
fun <T> Preference<T>.collectChangesWithCurrent(): State<T> = asState()
