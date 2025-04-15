package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.preferences.ThemeOption
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.jocmp.capy.Account

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

    var fontScale by mutableStateOf(appPreferences.articleListOptions.fontScale.get())
        private set

    val imagePreview: ImagePreview
        get() = _imagePreview.value

    val showSummary: Boolean
        get() = _showSummary.value

    val showFeedName: Boolean
        get() = _showFeedName.value

    val showFeedIcons: Boolean
        get() = _showFeedIcons.value

    var enableHighContrastDarkTheme by mutableStateOf(appPreferences.enableHighContrastDarkTheme.get())
        private set

    var pinArticleBars by mutableStateOf(appPreferences.readerOptions.pinToolbars.get())
        private set

    var imageVisibility by mutableStateOf(appPreferences.readerOptions.imageVisibility.get())
        private set

    var layout by mutableStateOf(appPreferences.layout.get())
        private set

    val enablePinArticleBars = !appPreferences.readerOptions.improveTalkback.get()

    fun updateTheme(theme: ThemeOption) {
        appPreferences.theme.set(theme)

        this.theme = theme
    }

    fun updateHighContrastDarkTheme(enable: Boolean) {
        appPreferences.enableHighContrastDarkTheme.set(enable)

        this.enableHighContrastDarkTheme = enable
    }

    fun updatePinArticleBars(pinBars: Boolean) {
        appPreferences.readerOptions.pinToolbars.set(pinBars)

        this.pinArticleBars = pinBars
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

    fun updateImageVisibility(option: ReaderImageVisibility) {
        appPreferences.readerOptions.imageVisibility.set(option)

        this.imageVisibility = option
    }

    fun updateLayoutPreference(layout: LayoutPreference) {
        appPreferences.layout.set(layout)

        this.layout = layout
    }

    fun updateFeedIcons(show: Boolean) {
        appPreferences.articleListOptions.showFeedIcons.set(show)

        _showFeedIcons.value = show
    }

    fun updateFeedName(show: Boolean) {
        appPreferences.articleListOptions.showFeedName.set(show)

        _showFeedName.value = show
    }
}
