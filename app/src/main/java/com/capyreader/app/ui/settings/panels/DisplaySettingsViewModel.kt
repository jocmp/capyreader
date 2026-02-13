package com.capyreader.app.ui.settings.panels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.sync.ReadingTimeWorker
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.jocmp.capy.Account

class DisplaySettingsViewModel(
    private val context: Context,
    val account: Account,
    val appPreferences: AppPreferences,
) : ViewModel() {
    var themeMode by mutableStateOf(appPreferences.themeMode.get())
        private set
    
    var appTheme by mutableStateOf(appPreferences.appTheme.get())
        private set
    
    var pureBlackDarkMode by mutableStateOf(appPreferences.pureBlackDarkMode.get())
        private set

    private val _imagePreview = mutableStateOf(appPreferences.articleListOptions.imagePreview.get())

    private val _showSummary = mutableStateOf(appPreferences.articleListOptions.showSummary.get())

    private val _showFeedName = mutableStateOf(appPreferences.articleListOptions.showFeedName.get())

    private val _showFeedIcons =
        mutableStateOf(appPreferences.articleListOptions.showFeedIcons.get())

    private val _shortenTitles = mutableStateOf(appPreferences.articleListOptions.shortenTitles.get())

    private val _showReadingTime =
        mutableStateOf(appPreferences.articleListOptions.showReadingTime.get())

    val showReadingTime: Boolean
        get() = _showReadingTime.value

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

    val shortenTitles: Boolean
        get() = _shortenTitles.value

    val pinArticleBars = appPreferences.readerOptions.pinToolbars

    var imageVisibility by mutableStateOf(appPreferences.readerOptions.imageVisibility.get())
        private set

    var layout by mutableStateOf(appPreferences.layout.get())
        private set

    val improveTalkback = appPreferences.readerOptions.improveTalkback

    val markReadButtonPosition = appPreferences.articleListOptions.markReadButtonPosition

    fun updateThemeMode(themeMode: ThemeMode) {
        appPreferences.themeMode.set(themeMode)
        this.themeMode = themeMode
    }

    fun updateAppTheme(appTheme: AppTheme) {
        appPreferences.appTheme.set(appTheme)
        this.appTheme = appTheme
    }

    fun updatePureBlackDarkMode(enable: Boolean) {
        appPreferences.pureBlackDarkMode.set(enable)
        this.pureBlackDarkMode = enable
    }

    fun updatePinArticleBars(pinBars: Boolean) {
        appPreferences.readerOptions.pinToolbars.set(pinBars)
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

    fun updateMarkReadButtonPosition(position: MarkReadPosition) {
        appPreferences.articleListOptions.markReadButtonPosition.set(position)
    }

    fun updateFeedIcons(show: Boolean) {
        appPreferences.articleListOptions.showFeedIcons.set(show)

        _showFeedIcons.value = show
    }

    fun updateFeedName(show: Boolean) {
        appPreferences.articleListOptions.showFeedName.set(show)

        _showFeedName.value = show
    }

    fun updateShortenTitles(shortenTitles: Boolean) {
        appPreferences.articleListOptions.shortenTitles.set(shortenTitles)

        _shortenTitles.value = shortenTitles
    }

    fun updateShowReadingTime(show: Boolean) {
        appPreferences.articleListOptions.showReadingTime.set(show)
        account.preferences.showReadingTime.set(show)

        _showReadingTime.value = show

        if (show) {
            backfillReadingTime()
        }
    }

    private fun backfillReadingTime() {
        val request = OneTimeWorkRequestBuilder<ReadingTimeWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(READING_TIME_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        private const val READING_TIME_WORK = "reading_time_backfill"
    }
}
