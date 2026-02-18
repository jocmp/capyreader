package com.capyreader.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import org.koin.compose.koinInject

@Composable
fun rememberTalkbackPreference(appPreferences: AppPreferences = koinInject()): State<Boolean> {
    return appPreferences.readerOptions.improveTalkback.collectChangesWithDefault()
}
