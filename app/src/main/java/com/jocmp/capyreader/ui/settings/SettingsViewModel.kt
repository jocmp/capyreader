package com.jocmp.capyreader.ui.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.refresher.RefreshInterval
import com.jocmp.capyreader.refresher.RefreshScheduler
import com.jocmp.capyreader.transfers.OPMLImportWorker
import com.jocmp.capyreader.transfers.OPMLImportWorker.Companion.PROGRESS_CURRENT_COUNT
import com.jocmp.capyreader.transfers.OPMLImportWorker.Companion.PROGRESS_TOTAL
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

    private val _importProgress = mutableStateOf<ImportProgress?>(null)

    val importProgress: ImportProgress?
        get() = _importProgress.value

    val refreshInterval: RefreshInterval
        get() = _refreshInterval.value

    val accountSource: Source = account.source

    val accountName: String
        get() = account.preferences.username.get()

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        _refreshInterval.value = interval
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
