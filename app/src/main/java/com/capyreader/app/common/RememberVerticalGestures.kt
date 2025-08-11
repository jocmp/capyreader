package com.capyreader.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.capyreader.app.preferences.AppPreferences
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

@Composable
fun rememberTalkbackPreference(appPreferences: AppPreferences = koinInject()): State<Boolean> {
    return appPreferences.settings.map { it.improveTalkback }.collectAsState(false)
}
