package com.jocmp.capyreader.ui.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.refresher.RefreshInterval
import com.jocmp.capyreader.refresher.RefreshScheduler

class SettingsViewModel(
    private val accountManager: AccountManager,
    private val refreshScheduler: RefreshScheduler,
    private val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _refreshInterval = mutableStateOf(refreshScheduler.refreshInterval)

    val refreshInterval: RefreshInterval
        get() = _refreshInterval.value

    val accountName: String
        get() = account.preferences.username.get()

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        _refreshInterval.value = interval
    }

    fun logOut() {
        appPreferences.clearAll()
        accountManager.removeAccount(accountID = account.id)
    }
}
