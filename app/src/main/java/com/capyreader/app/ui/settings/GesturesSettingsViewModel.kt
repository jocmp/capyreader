package com.capyreader.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.capyreader.app.common.AppPreferences

class GesturesSettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    var topSwipe by mutableStateOf(readerOptions.topSwipeGesture.get())
        private set

    var bottomSwipe by mutableStateOf(readerOptions.bottomSwipeGesture.get())
        private set

    fun updateReaderTopSwipe(swipe: ArticleVerticalSwipe) {
        topSwipe = swipe

        readerOptions.topSwipeGesture.set(swipe)
    }

    fun updateReaderBottomSwipe(swipe: ArticleVerticalSwipe) {
        bottomSwipe = swipe

        readerOptions.bottomSwipeGesture.set(swipe)
    }

    val readerOptions: AppPreferences.ReaderOptions
        get() = appPreferences.readerOptions
}
