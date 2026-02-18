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
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.jocmp.capy.Account
import kotlinx.coroutines.launch

class DisplaySettingsViewModel(
    val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    var themeMode by mutableStateOf(appPreferences.themeMode.defaultValue())
        private set

    var appTheme by mutableStateOf(appPreferences.appTheme.defaultValue())
        private set

    var pureBlackDarkMode by mutableStateOf(appPreferences.pureBlackDarkMode.defaultValue())
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

    var pinArticleBars by mutableStateOf(appPreferences.readerOptions.pinToolbars.defaultValue())
        private set

    var imageVisibility by mutableStateOf(appPreferences.readerOptions.imageVisibility.defaultValue())
        private set

    var improveTalkback by mutableStateOf(appPreferences.readerOptions.improveTalkback.defaultValue())
        private set

    var markReadButtonPosition by mutableStateOf(appPreferences.articleListOptions.markReadButtonPosition.defaultValue())
        private set

    init {
        viewModelScope.launch {
            themeMode = appPreferences.themeMode.get()
            appTheme = appPreferences.appTheme.get()
            pureBlackDarkMode = appPreferences.pureBlackDarkMode.get()
            _imagePreview.value = appPreferences.articleListOptions.imagePreview.get()
            _showSummary.value = appPreferences.articleListOptions.showSummary.get()
            _showFeedName.value = appPreferences.articleListOptions.showFeedName.get()
            _showFeedIcons.value = appPreferences.articleListOptions.showFeedIcons.get()
            _shortenTitles.value = appPreferences.articleListOptions.shortenTitles.get()
            fontScale = appPreferences.articleListOptions.fontScale.get()
            imageVisibility = appPreferences.readerOptions.imageVisibility.get()
            pinArticleBars = appPreferences.readerOptions.pinToolbars.get()
            improveTalkback = appPreferences.readerOptions.improveTalkback.get()
            markReadButtonPosition = appPreferences.articleListOptions.markReadButtonPosition.get()
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { appPreferences.themeMode.set(themeMode) }
        this.themeMode = themeMode
    }

    fun updateAppTheme(appTheme: AppTheme) {
        viewModelScope.launch { appPreferences.appTheme.set(appTheme) }
        this.appTheme = appTheme
    }

    fun updatePureBlackDarkMode(enable: Boolean) {
        viewModelScope.launch { appPreferences.pureBlackDarkMode.set(enable) }
        this.pureBlackDarkMode = enable
    }

    fun updatePinArticleBars(pinBars: Boolean) {
        viewModelScope.launch { appPreferences.readerOptions.pinToolbars.set(pinBars) }
        this.pinArticleBars = pinBars
    }

    fun updateFontScale(fontScale: ArticleListFontScale) {
        viewModelScope.launch { appPreferences.articleListOptions.fontScale.set(fontScale) }

        this.fontScale = fontScale
    }

    fun updateImagePreview(imagePreview: ImagePreview) {
        viewModelScope.launch { appPreferences.articleListOptions.imagePreview.set(imagePreview) }

        _imagePreview.value = imagePreview
    }

    fun updateSummary(show: Boolean) {
        viewModelScope.launch { appPreferences.articleListOptions.showSummary.set(show) }

        _showSummary.value = show
    }

    fun updateImageVisibility(option: ReaderImageVisibility) {
        viewModelScope.launch { appPreferences.readerOptions.imageVisibility.set(option) }

        this.imageVisibility = option
    }

    fun updateMarkReadButtonPosition(position: MarkReadPosition) {
        viewModelScope.launch { appPreferences.articleListOptions.markReadButtonPosition.set(position) }
        this.markReadButtonPosition = position
    }

    fun updateFeedIcons(show: Boolean) {
        viewModelScope.launch { appPreferences.articleListOptions.showFeedIcons.set(show) }

        _showFeedIcons.value = show
    }

    fun updateFeedName(show: Boolean) {
        viewModelScope.launch { appPreferences.articleListOptions.showFeedName.set(show) }

        _showFeedName.value = show
    }

    fun updateShortenTitles(shortenTitles: Boolean) {
        viewModelScope.launch { appPreferences.articleListOptions.shortenTitles.set(shortenTitles) }

        _shortenTitles.value = shortenTitles
    }
}
