package com.capyreader.app.ui.articles.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.asState
import org.koin.compose.koinInject

@Composable
internal fun rememberMarkAllReadState(
    appPreferences: AppPreferences = koinInject(),
): State<Boolean> {
    return appPreferences.articleListOptions.confirmMarkAllRead.asState()
}
