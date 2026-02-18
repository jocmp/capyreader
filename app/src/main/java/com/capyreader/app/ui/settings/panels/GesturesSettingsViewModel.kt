package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleListVerticalSwipe
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.BackAction
import com.capyreader.app.preferences.RowSwipeOption
import kotlinx.coroutines.launch

class GesturesSettingsViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    var backAction by mutableStateOf(listOptions.backAction.defaultValue())
        private set

    var readerTopSwipe by mutableStateOf(readerOptions.topSwipeGesture.defaultValue())
        private set

    var rowSwipeStart by mutableStateOf(listOptions.swipeStart.defaultValue())
        private set

    var rowSwipeEnd by mutableStateOf(listOptions.swipeEnd.defaultValue())
        private set

    var listSwipeBottom by mutableStateOf(listOptions.swipeBottom.defaultValue())
        private set

    var readerBottomSwipe by mutableStateOf(readerOptions.bottomSwipeGesture.defaultValue())
        private set

    var enablePagingTapGesture by mutableStateOf(readerOptions.enablePagingTapGesture.defaultValue())
        private set

    var enableHorizontalPagination by mutableStateOf(readerOptions.enableHorizontaPagination.defaultValue())
        private set

    var improveTalkback by mutableStateOf(readerOptions.improveTalkback.defaultValue())
        private set

    init {
        viewModelScope.launch {
            backAction = listOptions.backAction.get()
            readerTopSwipe = readerOptions.topSwipeGesture.get()
            rowSwipeStart = listOptions.swipeStart.get()
            rowSwipeEnd = listOptions.swipeEnd.get()
            listSwipeBottom = listOptions.swipeBottom.get()
            readerBottomSwipe = readerOptions.bottomSwipeGesture.get()
            enablePagingTapGesture = readerOptions.enablePagingTapGesture.get()
            enableHorizontalPagination = readerOptions.enableHorizontaPagination.get()
            improveTalkback = readerOptions.improveTalkback.get()
        }
    }

    fun updateBackAction(action: BackAction) {
        backAction = action

        viewModelScope.launch { listOptions.backAction.set(action) }
    }

    fun updateImproveTalkback(improve: Boolean) {
        improveTalkback = improve

        viewModelScope.launch {
            readerOptions.improveTalkback.set(improve)

            if (improve) {
                readerOptions.pinToolbars.set(true)
            }
        }

        if (improve) {
            updateReaderTopSwipe(ArticleVerticalSwipe.DISABLED)
            updateReaderBottomSwipe(ArticleVerticalSwipe.DISABLED)
        } else {
            updateReaderTopSwipe(ArticleVerticalSwipe.topSwipeDefault)
            updateReaderBottomSwipe(ArticleVerticalSwipe.bottomSwipeDefault)
        }
    }

    fun updateReaderTopSwipe(swipe: ArticleVerticalSwipe) {
        readerTopSwipe = swipe

        viewModelScope.launch { readerOptions.topSwipeGesture.set(swipe) }
    }

    fun updateReaderBottomSwipe(swipe: ArticleVerticalSwipe) {
        readerBottomSwipe = swipe

        viewModelScope.launch { readerOptions.bottomSwipeGesture.set(swipe) }
    }

    fun updateRowSwipeStart(swipe: RowSwipeOption) {
        rowSwipeStart = swipe

        viewModelScope.launch { listOptions.swipeStart.set(swipe) }
    }

    fun updateHorizontalPagination(scroll: Boolean) {
        enableHorizontalPagination = scroll

        viewModelScope.launch { readerOptions.enableHorizontaPagination.set(scroll) }
    }

    fun updateRowSwipeEnd(swipe: RowSwipeOption) {
        rowSwipeEnd = swipe

        viewModelScope.launch { listOptions.swipeEnd.set(swipe) }
    }

    fun updateListSwipeBottom(swipe: ArticleListVerticalSwipe) {
        listSwipeBottom = swipe

        viewModelScope.launch { listOptions.swipeBottom.set(swipe) }
    }

    fun updatePagingTapGesture(enabled: Boolean) {
        enablePagingTapGesture = enabled

        viewModelScope.launch { readerOptions.enablePagingTapGesture.set(enabled) }
    }

    private val readerOptions: AppPreferences.ReaderOptions
        get() = appPreferences.readerOptions

    private val listOptions: AppPreferences.ArticleListOptions
        get() = appPreferences.articleListOptions
}
