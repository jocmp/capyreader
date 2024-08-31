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
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshScheduler
import com.capyreader.app.transfers.OPMLImportWorker
import com.capyreader.app.transfers.OPMLImportWorker.Companion.PROGRESS_CURRENT_COUNT
import com.capyreader.app.transfers.OPMLImportWorker.Companion.PROGRESS_TOTAL
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val accountManager: AccountManager,
    private val refreshScheduler: RefreshScheduler,
    val account: Account,
    private val appPreferences: AppPreferences,
    application: Application
) : AndroidViewModel(application) {
    var refreshInterval by mutableStateOf(refreshScheduler.refreshInterval)
        private set

    var autoDelete by mutableStateOf(account.preferences.autoDelete.get())
        private set

    var canOpenLinksInternally by mutableStateOf(appPreferences.openLinksInternally.get())
        private set

    var theme by mutableStateOf(appPreferences.theme.get())
        private set

    var importProgress by mutableStateOf<ImportProgress?>(null)
        private set

    private val _imagePreview = mutableStateOf(appPreferences.articleDisplay.imagePreview.get())

    private val _showSummary = mutableStateOf(appPreferences.articleDisplay.showSummary.get())

    private val _showFeedName = mutableStateOf(appPreferences.articleDisplay.showFeedName.get())

    private val _showFeedIcons = mutableStateOf(appPreferences.articleDisplay.showFeedIcons.get())

    var enableStickyFullContent by mutableStateOf(appPreferences.enableStickyFullContent.get())
        private set

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

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        this.refreshInterval = interval
    }

    fun updateAutoDelete(autoDelete: AutoDelete) {
        account.preferences.autoDelete.set(autoDelete)

        this.autoDelete = autoDelete
    }

    fun updateTheme(theme: ThemeOption) {
        appPreferences.theme.set(theme)

        this.theme = theme
    }

    fun updateOpenLinksInternally(openLinksInternally: Boolean) {
        appPreferences.openLinksInternally.set(openLinksInternally)

        this.canOpenLinksInternally = openLinksInternally
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

        enableStickyFullContent = enable

        if (!enable) {
            viewModelScope.launch(Dispatchers.IO) {
                account.clearStickyFullContent()
            }
        }
    }

    fun startOPMLImport(uri: Uri?) {
        uri ?: return

        val requestID = OPMLImportWorker.performAsync(applicationContext, uri)

        viewModelScope.launch {
            WorkManager.getInstance(applicationContext)
                .getWorkInfoByIdFlow(requestID)
                .collect { workInfo: WorkInfo? ->
                    if (workInfo == null || workInfo.state.isFinished) {
                        importProgress = null
                    } else {
                        val currentCount = workInfo.progress.getInt(PROGRESS_CURRENT_COUNT, 0)
                        val total = workInfo.progress.getInt(PROGRESS_TOTAL, 0)

                        importProgress = ImportProgress(
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
