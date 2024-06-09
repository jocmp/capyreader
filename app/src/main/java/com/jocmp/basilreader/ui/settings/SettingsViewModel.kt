package com.jocmp.basilreader.ui.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.common.AppPreferences
import com.jocmp.basilreader.refresher.RefreshInterval
import com.jocmp.basilreader.refresher.RefreshScheduler

class SettingsViewModel(
    private val accountManager: AccountManager,
    private val refreshScheduler: RefreshScheduler,
    private val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val _refreshInterval = mutableStateOf(refreshScheduler.refreshInterval)

    val displayName: String
        get() = "Feedbin"

    val refreshInterval: RefreshInterval
        get() = _refreshInterval.value

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        _refreshInterval.value = interval
    }

    fun logOut() {
        appPreferences.clearAll()
        accountManager.removeAccount(accountID = account.id)
    }
}
