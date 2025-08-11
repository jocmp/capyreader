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
import com.jocmp.capy.common.launchIO

class GesturesSettingsViewModel(
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

    var listSwipeBottom by mutableStateOf(listOptions.swipeBottom.get())
        private set

    var readerBottomSwipe by mutableStateOf(readerOptions.bottomSwipeGesture.get())
        private set

    var enablePagingTapGesture by mutableStateOf(readerOptions.enablePagingTapGesture.get())
        private set

    var enableHorizontalPagination by mutableStateOf(readerOptions.enableHorizontaPagination.get())
        private set

    var improveTalkback by mutableStateOf(readerOptions.improveTalkback.get())
        private set

    fun updateBackAction(action: BackAction) {
        backAction = action

        viewModelScope.launchIO {
            listOptions.backAction.set(action)
        }
    }

    fun updateImproveTalkback(improve: Boolean) {
        improveTalkback = improve

        viewModelScope.launchIO {
            readerOptions.improveTalkback.set(improve)

            if (improve) {
                readerOptions.pinTopToolbar.set(true)
                updateReaderTopSwipe(ArticleVerticalSwipe.DISABLED)
                updateReaderBottomSwipe(ArticleVerticalSwipe.DISABLED)
            } else {
                updateReaderTopSwipe(ArticleVerticalSwipe.topSwipeDefault)
                updateReaderBottomSwipe(ArticleVerticalSwipe.bottomSwipeDefault)
            }
        }
    }

    fun updateReaderTopSwipe(swipe: ArticleVerticalSwipe) {
        readerTopSwipe = swipe

        viewModelScope.launchIO {

            readerOptions.topSwipeGesture.set(swipe)
        }
    }

    fun updateReaderBottomSwipe(swipe: ArticleVerticalSwipe) {
        readerBottomSwipe = swipe

        viewModelScope.launchIO {
            readerOptions.bottomSwipeGesture.set(swipe)
        }
    }

    fun updateRowSwipeStart(swipe: RowSwipeOption) {
        rowSwipeStart = swipe

        viewModelScope.launchIO {
            listOptions.swipeStart.set(swipe)
        }
    }


    fun updateHorizontalPagination(scroll: Boolean) {
        enableHorizontalPagination = scroll

        viewModelScope.launchIO {
            readerOptions.enableHorizontaPagination.set(scroll)
        }
    }

    fun updateRowSwipeEnd(swipe: RowSwipeOption) {
        rowSwipeEnd = swipe

        viewModelScope.launchIO {
            listOptions.swipeEnd.set(swipe)
        }
    }

    fun updateListSwipeBottom(swipe: ArticleListVerticalSwipe) {
        listSwipeBottom = swipe

        viewModelScope.launchIO {
            listOptions.swipeBottom.set(swipe)
        }
    }

    fun updatePagingTapGesture(enabled: Boolean) {
        enablePagingTapGesture = enabled

        viewModelScope.launchIO {
            readerOptions.enablePagingTapGesture.set(enabled)
        }
    }

    private val readerOptions: AppPreferences.ReaderOptions
        get() = appPreferences.readerOptions

    private val listOptions: AppPreferences.ArticleListOptions
        get() = appPreferences.articleListOptions
}
