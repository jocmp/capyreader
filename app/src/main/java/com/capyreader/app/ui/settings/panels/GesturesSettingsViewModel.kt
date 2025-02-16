package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.BackAction
import com.capyreader.app.preferences.RowSwipeOption
import com.jocmp.capy.Account

class GesturesSettingsViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
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

    var enablePagingTapGesture by mutableStateOf(readerOptions.enablePagingTapGesture.get())
        private set

    var enableHorizontalPagination by mutableStateOf(readerOptions.enableHorizontaPagination.get())
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

    fun updateHorizontalPagination(scroll: Boolean) {
        enableHorizontalPagination = scroll

        readerOptions.enableHorizontaPagination.set(scroll)
    }

    fun updateRowSwipeEnd(swipe: RowSwipeOption) {
        rowSwipeEnd = swipe

        listOptions.swipeEnd.set(swipe)
    }

    fun updatePagingTapGesture(enabled: Boolean) {
        enablePagingTapGesture = enabled

        readerOptions.enablePagingTapGesture.set(enabled)
    }

    private val readerOptions: AppPreferences.ReaderOptions
        get() = appPreferences.readerOptions

    private val listOptions: AppPreferences.ArticleListOptions
        get() = appPreferences.articleListOptions
}
