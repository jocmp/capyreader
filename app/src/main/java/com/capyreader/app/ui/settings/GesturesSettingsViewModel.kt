package com.capyreader.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.BackAction

class GesturesSettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    var backAction by mutableStateOf(listOptions.backAction.get())
        private set

    var readerTopSwipe by mutableStateOf(readerOptions.topSwipeGesture.get())
        private set

    var rowSwipeStart by mutableStateOf(listOptions.swipeStart.get())
        private set

    var rowSwipeEnd by mutableStateOf(listOptions.swipeEnd.get())
        private set

    var readerBottomSwipe by mutableStateOf(readerOptions.bottomSwipeGesture.get())
        private set

    fun updateBackAction(action: BackAction) {
        backAction = action

        listOptions.backAction.set(action)
    }

    fun updateReaderTopSwipe(swipe: ArticleVerticalSwipe) {
        readerTopSwipe = swipe

        readerOptions.topSwipeGesture.set(swipe)
    }

    fun updateReaderBottomSwipe(swipe: ArticleVerticalSwipe) {
        readerBottomSwipe = swipe

        readerOptions.bottomSwipeGesture.set(swipe)
    }

    fun updateRowSwipeStart(swipe: RowSwipeOption) {
        rowSwipeStart = swipe

        listOptions.swipeStart.set(swipe)
    }

    fun updateRowSwipeEnd(swipe: RowSwipeOption) {
        rowSwipeEnd = swipe

        listOptions.swipeEnd.set(swipe)
    }

    private val readerOptions: AppPreferences.ReaderOptions
        get() = appPreferences.readerOptions

    private val listOptions: AppPreferences.ArticleListOptions
        get() = appPreferences.articleListOptions
}
