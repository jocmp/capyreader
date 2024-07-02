package com.jocmp.capyreader.ui.settings

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.refresher.RefreshInterval
import com.jocmp.capyreader.refresher.RefreshScheduler
import com.jocmp.capyreader.transfers.OPMLExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val accountManager: AccountManager,
    private val refreshScheduler: RefreshScheduler,
    val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _refreshInterval = mutableStateOf(refreshScheduler.refreshInterval)

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

    fun importOPML() {
    }
}
