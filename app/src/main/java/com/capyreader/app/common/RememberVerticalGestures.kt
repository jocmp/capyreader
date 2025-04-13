package com.capyreader.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.ui.collectChangesWithDefault
import org.koin.compose.koinInject

@Composable
fun rememberTalkbackPreference(appPreferences: AppPreferences = koinInject()): Boolean {
    val topSwipeGesture by appPreferences.readerOptions.topSwipeGesture.collectChangesWithDefault()
    val bottomSwipeGesture by appPreferences.readerOptions.bottomSwipeGesture.collectChangesWithDefault()
    val pagingTapGesture by appPreferences.readerOptions.enablePagingTapGesture.collectChangesWithDefault()

    return topSwipeGesture != ArticleVerticalSwipe.DISABLED ||
            bottomSwipeGesture != ArticleVerticalSwipe.DISABLED ||
            pagingTapGesture
}
