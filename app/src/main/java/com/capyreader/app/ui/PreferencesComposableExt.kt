package com.capyreader.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.jocmp.capy.preferences.Preference

@Composable
fun <T> Preference<T>.collectChangesWithDefault(): State<T> = changes().collectAsState(initial = defaultValue())
