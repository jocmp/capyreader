package com.capyreader.app.ui.settings

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshScheduler
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source

class GeneralSettingsViewModel(
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

    val accountSource: Source = account.source

    val accountName: String
        get() = account.preferences.username.get()

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        this.refreshInterval = interval
    }

    fun updateAutoDelete(autoDelete: AutoDelete) {
        account.preferences.autoDelete.set(autoDelete)

        this.autoDelete = autoDelete
    }

    fun updateOpenLinksInternally(openLinksInternally: Boolean) {
        appPreferences.openLinksInternally.set(openLinksInternally)

        this.canOpenLinksInternally = openLinksInternally
    }

    fun clearAllArticles() {
        account.clearAllArticles()
    }

    fun removeAccount() {
        appPreferences.clearAll()
        accountManager.removeAccount(accountID = account.id)
    }

    private val applicationContext: Context
        get() = getApplication<Application>().applicationContext
}
