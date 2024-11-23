package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.BackAction
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    var enableStickyFullContent by mutableStateOf(appPreferences.enableStickyFullContent.get())
        private set

    var pinArticleTopBar by mutableStateOf(appPreferences.readerOptions.pinToolbars.get())
        private set

    var confirmMarkAllRead by mutableStateOf(appPreferences.articleListOptions.confirmMarkAllRead.get())
        private set

    var markReadOnScroll by mutableStateOf(appPreferences.articleListOptions.markReadOnScroll.get())
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

    fun updatePagingTapGesture(enabled: Boolean) {
        enablePagingTapGesture = enabled

        readerOptions.enablePagingTapGesture.set(enabled)
    }

    fun updatePinTopBar(pinTopBar: Boolean) {
        appPreferences.readerOptions.pinToolbars.set(pinTopBar)

        this.pinArticleTopBar = pinTopBar
    }

    fun updateStickyFullContent(enable: Boolean) {
        appPreferences.enableStickyFullContent.set(enable)

        enableStickyFullContent = enable

        if (!enable) {
            viewModelScope.launch(Dispatchers.IO) {
                account.clearStickyFullContent()
            }
        }
    }

    fun updateConfirmMarkAllRead(confirm: Boolean) {
        appPreferences.articleListOptions.confirmMarkAllRead.set(confirm)

        confirmMarkAllRead = confirm
    }

    fun updateMarkReadOnScroll(enable: Boolean) {
        appPreferences.articleListOptions.markReadOnScroll.set(enable)

        markReadOnScroll = enable
    }

    private val readerOptions: AppPreferences.ReaderOptions
        get() = appPreferences.readerOptions

    private val listOptions: AppPreferences.ArticleListOptions
        get() = appPreferences.articleListOptions
}
