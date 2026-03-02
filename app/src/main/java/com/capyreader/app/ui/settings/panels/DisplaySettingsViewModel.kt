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

    private val _imagePreview = mutableStateOf(appPreferences.articleListOptions.imagePreview.defaultValue())

    private val _showSummary = mutableStateOf(appPreferences.articleListOptions.showSummary.defaultValue())

    private val _showFeedName = mutableStateOf(appPreferences.articleListOptions.showFeedName.defaultValue())

    private val _showFeedIcons =
        mutableStateOf(appPreferences.articleListOptions.showFeedIcons.defaultValue())

    private val _shortenTitles = mutableStateOf(appPreferences.articleListOptions.shortenTitles.defaultValue())

    var fontScale by mutableStateOf(appPreferences.articleListOptions.fontScale.defaultValue())
        private set

    val imagePreview: ImagePreview
        get() = _imagePreview.value

    val showSummary: Boolean
        get() = _showSummary.value

    val showFeedName: Boolean
        get() = _showFeedName.value

    val showFeedIcons: Boolean
        get() = _showFeedIcons.value

    val shortenTitles: Boolean
        get() = _shortenTitles.value

    val pinArticleBars = appPreferences.readerOptions.pinToolbars

    var imageVisibility by mutableStateOf(appPreferences.readerOptions.imageVisibility.defaultValue())
        private set

    val improveTalkback = appPreferences.readerOptions.improveTalkback

    val markReadButtonPosition = appPreferences.articleListOptions.markReadButtonPosition

    init {
        viewModelScope.launch {
            themeMode = appPreferences.themeMode.get()
            appTheme = appPreferences.appTheme.get()
            pureBlackDarkMode = appPreferences.pureBlackDarkMode.get()
            accentColors = appPreferences.accentColors.get()
            _imagePreview.value = appPreferences.articleListOptions.imagePreview.get()
            _showSummary.value = appPreferences.articleListOptions.showSummary.get()
            _showFeedName.value = appPreferences.articleListOptions.showFeedName.get()
            _showFeedIcons.value = appPreferences.articleListOptions.showFeedIcons.get()
            _shortenTitles.value = appPreferences.articleListOptions.shortenTitles.get()
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
        _imagePreview.value = imagePreview
        viewModelScope.launch { appPreferences.articleListOptions.imagePreview.set(imagePreview) }
    }

    fun updateSummary(show: Boolean) {
        _showSummary.value = show
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
        _showFeedIcons.value = show
        viewModelScope.launch { appPreferences.articleListOptions.showFeedIcons.set(show) }
    }

    fun updateFeedName(show: Boolean) {
        _showFeedName.value = show
        viewModelScope.launch { appPreferences.articleListOptions.showFeedName.set(show) }
    }

    fun updateShortenTitles(shortenTitles: Boolean) {
        _shortenTitles.value = shortenTitles
        viewModelScope.launch { appPreferences.articleListOptions.shortenTitles.set(shortenTitles) }
    }
}
