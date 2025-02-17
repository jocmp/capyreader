package com.capyreader.app.ui.settings.panels

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
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.transfers.OPMLImportWorker
import com.capyreader.app.transfers.OPMLImportWorker.Companion.PROGRESS_CURRENT_COUNT
import com.capyreader.app.transfers.OPMLImportWorker.Companion.PROGRESS_TOTAL
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import kotlinx.coroutines.launch

class AccountSettingsViewModel(
    private val accountManager: AccountManager,
    val account: Account,
    private val appPreferences: AppPreferences,
    application: Application
) : AndroidViewModel(application) {
    val accountSource: Source = account.source

    var importProgress by mutableStateOf<ImportProgress?>(null)
        private set

    val accountURL = account.preferences.url.get()

    val accountName = account.preferences.username.get()

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
