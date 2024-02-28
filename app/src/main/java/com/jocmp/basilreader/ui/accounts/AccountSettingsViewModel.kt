package com.jocmp.basilreader.ui.accounts

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.refresher.RefreshInterval
import com.jocmp.basilreader.refresher.RefreshScheduler
import kotlinx.coroutines.launch
import java.io.InputStream

class AccountSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AccountManager,
    private val refreshScheduler: RefreshScheduler,
) : ViewModel() {
    private val args = AccountSettingsArgs(savedStateHandle)

    private val _account = mutableStateOf(
        accountManager.findByID(args.accountID)!!,
        policy = neverEqualPolicy()
    )

    private val _refreshInterval = mutableStateOf(refreshScheduler.refreshInterval)

    val account: Account
        get() = _account.value

    val displayName: String
        get() = "Feedbin"

    val refreshInterval: RefreshInterval
        get() = _refreshInterval.value

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        _refreshInterval.value = interval
    }

    fun removeAccount() {
        accountManager.removeAccount(accountID = account.id)
    }
}
