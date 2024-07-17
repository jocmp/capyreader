package com.capyreader.app.ui.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshScheduler
import com.capyreader.app.transfers.OPMLImportWorker
import com.capyreader.app.transfers.OPMLImportWorker.Companion.PROGRESS_CURRENT_COUNT
import com.capyreader.app.transfers.OPMLImportWorker.Companion.PROGRESS_TOTAL
import kotlinx.coroutines.launch

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val accountManager: AccountManager,
    private val refreshScheduler: RefreshScheduler,
    val account: Account,
    private val appPreferences: AppPreferences,
    application: Application
) : AndroidViewModel(application) {
    private val _refreshInterval = mutableStateOf(refreshScheduler.refreshInterval)

    private val _openLinksInternally = mutableStateOf(appPreferences.openLinksInternally.get())

    private val _theme = mutableStateOf(appPreferences.theme.get())

    private val _importProgress = mutableStateOf<ImportProgress?>(null)

    private val _imagePreview = mutableStateOf(appPreferences.articleDisplay.imagePreview.get())

    private val _showSummary = mutableStateOf(appPreferences.articleDisplay.showSummary.get())

    private val _showFeedName = mutableStateOf(appPreferences.articleDisplay.showFeedName.get())

    private val _showFeedIcons = mutableStateOf(appPreferences.articleDisplay.showFeedIcons.get())

    private val _enableStickyFullContent =
        mutableStateOf(appPreferences.enableStickyFullContent.get())

    val importProgress: ImportProgress?
        get() = _importProgress.value

    val refreshInterval: RefreshInterval
        get() = _refreshInterval.value

    val theme: ThemeOption
        get() = _theme.value

    val canOpenLinksInternally: Boolean
        get() = _openLinksInternally.value

    val accountSource: Source = account.source

    val accountName: String
        get() = account.preferences.username.get()

    val imagePreview: ImagePreview
        get() = _imagePreview.value

    val showSummary: Boolean
        get() = _showSummary.value

    val showFeedName: Boolean
        get() = _showFeedName.value

    val showFeedIcons: Boolean
        get() = _showFeedIcons.value

    val enableStickyFullContent: Boolean
        get() = _enableStickyFullContent.value

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        _refreshInterval.value = interval
    }

    fun updateTheme(theme: ThemeOption) {
        appPreferences.theme.set(theme)

        _theme.value = theme
    }

    fun updateOpenLinksInternally(openLinksInternally: Boolean) {
        appPreferences.openLinksInternally.set(openLinksInternally)

        _openLinksInternally.value = openLinksInternally
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

    fun updateStickyFullContent(enable: Boolean) {
        appPreferences.enableStickyFullContent.set(enable)

        _enableStickyFullContent.value = enable
    }

    fun removeAccount() {
        appPreferences.clearAll()
        accountManager.removeAccount(accountID = account.id)
    }

    fun startOPMLImport(uri: Uri?) {
        uri ?: return

        val requestID = OPMLImportWorker.performAsync(applicationContext, uri)

        viewModelScope.launch {
            WorkManager.getInstance(applicationContext)
                .getWorkInfoByIdFlow(requestID)
                .collect { workInfo: WorkInfo? ->
                    if (workInfo == null || workInfo.state.isFinished) {
                        _importProgress.value = null
                    } else {
                        val currentCount = workInfo.progress.getInt(PROGRESS_CURRENT_COUNT, 0)
                        val total = workInfo.progress.getInt(PROGRESS_TOTAL, 0)

                        _importProgress.value = ImportProgress(
                            currentCount = currentCount,
                            total = total
                        )
                    }
                }
        }
    }

    private val applicationContext: Context
        get() = getApplication<Application>().applicationContext
}
