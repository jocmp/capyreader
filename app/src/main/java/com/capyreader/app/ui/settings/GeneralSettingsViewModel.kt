package com.capyreader.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshScheduler
import com.jocmp.capy.Account
import com.jocmp.capy.accounts.AutoDelete

class GeneralSettingsViewModel(
    private val refreshScheduler: RefreshScheduler,
    val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
    var refreshInterval by mutableStateOf(refreshScheduler.refreshInterval)
        private set

    var autoDelete by mutableStateOf(account.preferences.autoDelete.get())
        private set

    var canOpenLinksInternally by mutableStateOf(appPreferences.openLinksInternally.get())
        private set

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
}
