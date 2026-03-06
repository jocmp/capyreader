package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.jocmp.capy.Account
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DisplaySettingsViewModel(
    val account: Account,
    val appPreferences: AppPreferences,
) : ViewModel() {
    var themeMode by mutableStateOf(appPreferences.themeMode.defaultValue())
        private set

    var appTheme by mutableStateOf(appPreferences.appTheme.defaultValue())
        private set

    var pureBlackDarkMode by mutableStateOf(appPreferences.pureBlackDarkMode.defaultValue())
        private set

    var accentColors by mutableStateOf(appPreferences.accentColors.defaultValue())
        private set

    var imagePreview by mutableStateOf(appPreferences.articleListOptions.imagePreview.defaultValue())
        private set

    var showSummary by mutableStateOf(appPreferences.articleListOptions.showSummary.defaultValue())
        private set

    var showFeedName by mutableStateOf(appPreferences.articleListOptions.showFeedName.defaultValue())
        private set

    var showFeedIcons by mutableStateOf(appPreferences.articleListOptions.showFeedIcons.defaultValue())
        private set

    var shortenTitles by mutableStateOf(appPreferences.articleListOptions.shortenTitles.defaultValue())
        private set

    var fontScale by mutableStateOf(appPreferences.articleListOptions.fontScale.defaultValue())
        private set

    val pinArticleBars = appPreferences.readerOptions.pinToolbars

    var imageVisibility by mutableStateOf(appPreferences.readerOptions.imageVisibility.defaultValue())
        private set

    val improveTalkback = appPreferences.readerOptions.improveTalkback

    val markReadButtonPosition = appPreferences.articleListOptions.markReadButtonPosition

    init {
        runBlocking {
            themeMode = appPreferences.themeMode.get()
            appTheme = appPreferences.appTheme.get()
            pureBlackDarkMode = appPreferences.pureBlackDarkMode.get()
            accentColors = appPreferences.accentColors.get()
            imagePreview = appPreferences.articleListOptions.imagePreview.get()
            showSummary = appPreferences.articleListOptions.showSummary.get()
            showFeedName = appPreferences.articleListOptions.showFeedName.get()
            showFeedIcons = appPreferences.articleListOptions.showFeedIcons.get()
            shortenTitles = appPreferences.articleListOptions.shortenTitles.get()
            fontScale = appPreferences.articleListOptions.fontScale.get()
            imageVisibility = appPreferences.readerOptions.imageVisibility.get()
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        this.themeMode = themeMode
        viewModelScope.launch { appPreferences.themeMode.set(themeMode) }
    }

    fun updatePureBlackDarkMode(enable: Boolean) {
        this.pureBlackDarkMode = enable
        viewModelScope.launch { appPreferences.pureBlackDarkMode.set(enable) }
    }

    fun updateAccentColors(enable: Boolean) {
        this.accentColors = enable
        viewModelScope.launch { appPreferences.accentColors.set(enable) }
    }

    fun updatePinArticleBars(pinBars: Boolean) {
        viewModelScope.launch { appPreferences.readerOptions.pinToolbars.set(pinBars) }
    }

    fun updateFontScale(fontScale: ArticleListFontScale) {
        this.fontScale = fontScale
        viewModelScope.launch { appPreferences.articleListOptions.fontScale.set(fontScale) }
    }

    fun updateImagePreview(imagePreview: ImagePreview) {
        this.imagePreview = imagePreview
        viewModelScope.launch { appPreferences.articleListOptions.imagePreview.set(imagePreview) }
    }

    fun updateSummary(show: Boolean) {
        this.showSummary = show
        viewModelScope.launch { appPreferences.articleListOptions.showSummary.set(show) }
    }

    fun updateImageVisibility(option: ReaderImageVisibility) {
        this.imageVisibility = option
        viewModelScope.launch { appPreferences.readerOptions.imageVisibility.set(option) }
    }

    fun updateMarkReadButtonPosition(position: MarkReadPosition) {
        viewModelScope.launch { appPreferences.articleListOptions.markReadButtonPosition.set(position) }
    }

    fun updateFeedIcons(show: Boolean) {
        this.showFeedIcons = show
        viewModelScope.launch { appPreferences.articleListOptions.showFeedIcons.set(show) }
    }

    fun updateFeedName(show: Boolean) {
        this.showFeedName = show
        viewModelScope.launch { appPreferences.articleListOptions.showFeedName.set(show) }
    }

    fun updateShortenTitles(shortenTitles: Boolean) {
        this.shortenTitles = shortenTitles
        viewModelScope.launch { appPreferences.articleListOptions.shortenTitles.set(shortenTitles) }
    }
}
