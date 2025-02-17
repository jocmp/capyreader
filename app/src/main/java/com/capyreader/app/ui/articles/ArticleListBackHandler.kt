package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.BackAction
import com.capyreader.app.common.asState
import org.koin.compose.koinInject

@Composable
fun ArticleListBackHandler(
    appPreferences: AppPreferences = koinInject(),
    closeDrawer: () -> Unit,
    toggleDrawer: () -> Unit,
    enabled: Boolean,
    isDrawerOpen: Boolean,
) {
    val backAction by appPreferences.articleListOptions.backAction.asState()

    BackHandler(enabled && backAction != BackAction.SYSTEM_BACK) {
        toggleDrawer()
    }

    BackHandler(enabled && isDrawerOpen && backAction == BackAction.SYSTEM_BACK) {
        closeDrawer()
    }
}
