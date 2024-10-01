package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.BackAction
import com.capyreader.app.common.asState
import org.koin.compose.koinInject

@Composable
fun ArticleListBackHandler(
    appPreferences: AppPreferences = koinInject(),
    enabled: Boolean,
    onBack: () -> Unit,
) {
    val backAction by appPreferences.articleListOptions.backAction.asState()

    BackHandler(enabled && backAction != BackAction.SYSTEM_BACK) {
        onBack()
    }
}
