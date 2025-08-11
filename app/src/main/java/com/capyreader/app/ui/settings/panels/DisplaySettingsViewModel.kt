package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.preferences.ThemeOption
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.jocmp.capy.Account
import com.jocmp.capy.common.launchIO

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

    private val _shortenTitles =
        mutableStateOf(appPreferences.articleListOptions.shortenTitles.get())

    var fontScale by mutableStateOf(appPreferences.articleListOptions.fontScale.get())
        private set

    var enableBottomBarActions = appPreferences.readerOptions.bottomBarActions

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

    var enableHighContrastDarkTheme by mutableStateOf(appPreferences.enableHighContrastDarkTheme.get())
        private set

    val pinArticleBars = appPreferences.readerOptions.pinTopToolbar

    var imageVisibility by mutableStateOf(appPreferences.readerOptions.imageVisibility.get())
        private set

    var layout by mutableStateOf(appPreferences.layout.get())
        private set

    val improveTalkback = appPreferences.readerOptions.improveTalkback

    val markReadButtonPosition = appPreferences.articleListOptions.markReadButtonPosition

    fun updateTheme(theme: ThemeOption) {
        viewModelScope.launchIO {
            appPreferences.theme.set(theme)
        }

        this.theme = theme
    }

    fun updateHighContrastDarkTheme(enable: Boolean) {
        viewModelScope.launchIO {
            appPreferences.enableHighContrastDarkTheme.set(enable)
        }

        this.enableHighContrastDarkTheme = enable
    }

    fun updatePinArticleBars(pinBars: Boolean) {
        viewModelScope.launchIO {
            appPreferences.readerOptions.pinTopToolbar.set(pinBars)
        }
    }

    fun updateBottomBarActions(enable: Boolean) {
        viewModelScope.launchIO {
            appPreferences.readerOptions.bottomBarActions.set(enable)
        }
    }

    fun updateFontScale(fontScale: ArticleListFontScale) {
        viewModelScope.launchIO {
            appPreferences.articleListOptions.fontScale.set(fontScale)
        }

        this.fontScale = fontScale
    }

    fun updateImagePreview(imagePreview: ImagePreview) {
        viewModelScope.launchIO {
            appPreferences.articleListOptions.imagePreview.set(imagePreview)
        }

        _imagePreview.value = imagePreview
    }

    fun updateSummary(show: Boolean) {
        viewModelScope.launchIO {
            appPreferences.articleListOptions.showSummary.set(show)
        }

        _showSummary.value = show
    }

    fun updateImageVisibility(option: ReaderImageVisibility) {
        viewModelScope.launchIO {
            appPreferences.readerOptions.imageVisibility.set(option)
        }

        this.imageVisibility = option
    }

    fun updateLayoutPreference(layout: LayoutPreference) {
        viewModelScope.launchIO {
            appPreferences.layout.set(layout)
        }

        this.layout = layout
    }

    fun updateMarkReadButtonPosition(position: MarkReadPosition) {
        viewModelScope.launchIO {
            appPreferences.articleListOptions.markReadButtonPosition.set(position)
        }
    }

    fun updateFeedIcons(show: Boolean) {
        viewModelScope.launchIO {
            appPreferences.articleListOptions.showFeedIcons.set(show)
        }

        _showFeedIcons.value = show
    }

    fun updateFeedName(show: Boolean) {
        viewModelScope.launchIO {
            appPreferences.articleListOptions.showFeedName.set(show)
        }

        _showFeedName.value = show
    }

    fun updateShortenTitles(shortenTitles: Boolean) {
        viewModelScope.launchIO {
            appPreferences.articleListOptions.shortenTitles.set(shortenTitles)
        }

        _shortenTitles.value = shortenTitles
    }
}
