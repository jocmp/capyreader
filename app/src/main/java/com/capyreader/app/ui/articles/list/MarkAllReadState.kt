package com.capyreader.app.ui.articles.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.asState
import org.koin.compose.koinInject


internal data class MarkAllReadState(
    val confirmationEnabled: Boolean,
)
@Composable
internal fun rememberMarkAllReadState(
    appPreferences: AppPreferences = koinInject(),
): MarkAllReadState {
    val confirmationEnabled by appPreferences.articleListOptions.confirmMarkAllRead.asState()

    return remember { MarkAllReadState(confirmationEnabled) }
}