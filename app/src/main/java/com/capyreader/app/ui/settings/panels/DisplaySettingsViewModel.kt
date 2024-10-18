package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DisplaySettingsViewModel(
    val account: Account,
    val appPreferences: AppPreferences,
) : ViewModel() {
    var theme by mutableStateOf(appPreferences.theme.get())
        private set

    private val _imagePreview = mutableStateOf(appPreferences.articleListOptions.imagePreview.get())

    private val _showSummary = mutableStateOf(appPreferences.articleListOptions.showSummary.get())

    private val _showFeedName = mutableStateOf(appPreferences.articleListOptions.showFeedName.get())

    private val _showFeedIcons =
        mutableStateOf(appPreferences.articleListOptions.showFeedIcons.get())

    private val _confirmMarkAllRead =
        mutableStateOf(appPreferences.articleListOptions.confirmMarkAllRead.get())

    var fontScale by mutableStateOf(appPreferences.articleListOptions.fontScale.get())
        private set

    var pinArticleTopBar by mutableStateOf(appPreferences.readerOptions.pinToolbars.get())
        private set

    val imagePreview: ImagePreview
        get() = _imagePreview.value

    val showSummary: Boolean
        get() = _showSummary.value

    val showFeedName: Boolean
        get() = _showFeedName.value

    val showFeedIcons: Boolean
        get() = _showFeedIcons.value

    val confirmMarkAllRead: Boolean
        get() = _confirmMarkAllRead.value

    var enableStickyFullContent by mutableStateOf(appPreferences.enableStickyFullContent.get())
        private set

    fun updateTheme(theme: ThemeOption) {
        appPreferences.theme.set(theme)

        this.theme = theme
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

    fun updateFontScale(fontScale: ArticleListFontScale) {
        appPreferences.articleListOptions.fontScale.set(fontScale)

        this.fontScale = fontScale
    }

    fun updateImagePreview(imagePreview: ImagePreview) {
        appPreferences.articleListOptions.imagePreview.set(imagePreview)

        _imagePreview.value = imagePreview
    }

    fun updateSummary(show: Boolean) {
        appPreferences.articleListOptions.showSummary.set(show)

        _showSummary.value = show
    }

    fun updateFeedIcons(show: Boolean) {
        appPreferences.articleListOptions.showFeedIcons.set(show)

        _showFeedIcons.value = show
    }

    fun updateFeedName(show: Boolean) {
        appPreferences.articleListOptions.showFeedName.set(show)

        _showFeedName.value = show
    }

    fun updateConfirmMarkAllRead(confirm: Boolean) {
        appPreferences.articleListOptions.confirmMarkAllRead.set(confirm)

        _confirmMarkAllRead.value = confirm
    }
}
