package com.capyreader.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.ThemeOption
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DisplaySettingsViewModel(
    val account: Account,
    val appPreferences: AppPreferences,
): ViewModel() {
    var theme by mutableStateOf(appPreferences.theme.get())
        private set

    private val _imagePreview = mutableStateOf(appPreferences.articleDisplay.imagePreview.get())

    private val _showSummary = mutableStateOf(appPreferences.articleDisplay.showSummary.get())

    private val _showFeedName = mutableStateOf(appPreferences.articleDisplay.showFeedName.get())

    private val _showFeedIcons = mutableStateOf(appPreferences.articleDisplay.showFeedIcons.get())

    val imagePreview: ImagePreview
        get() = _imagePreview.value

    val showSummary: Boolean
        get() = _showSummary.value

    val showFeedName: Boolean
        get() = _showFeedName.value

    val showFeedIcons: Boolean
        get() = _showFeedIcons.value

    var enableStickyFullContent by mutableStateOf(appPreferences.enableStickyFullContent.get())
        private set

    fun updateTheme(theme: ThemeOption) {
        appPreferences.theme.set(theme)

        this.theme = theme
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

    fun updateImagePreview(imagePreview: ImagePreview) {
        appPreferences.articleDisplay.imagePreview.set(imagePreview)

        _imagePreview.value = imagePreview
    }

    fun updateSummary(show: Boolean) {
        appPreferences.articleDisplay.showSummary.set(show)

        _showSummary.value = show
    }

    fun updateFeedIcons(show: Boolean) {
        appPreferences.articleDisplay.showFeedIcons.set(show)

        _showFeedIcons.value = show
    }

    fun updateFeedName(show: Boolean) {
        appPreferences.articleDisplay.showFeedName.set(show)

        _showFeedName.value = show
    }
}
