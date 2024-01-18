package com.jocmp.basilreader.ui.accounts

import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.putAccountID
import kotlinx.coroutines.launch

class AccountIndexViewModel(
    private val accountManager: AccountManager,
    private val settings: DataStore<Preferences>,
) : ViewModel() {
    private val _accounts = mutableStateListOf<Account>().apply {
        addAll(accountManager.accounts)
    }

    val accounts: List<Account>
        get() = _accounts.toList()

    fun createAccount() {
        viewModelScope.launch {
            _accounts.add(accountManager.createAccount())
        }
    }

    suspend fun selectAccount(id: String) {
        settings.putAccountID(id)
    }
//
//    fun removeAccount(id: String) {
//        if (!accountManager.removeAccount(id)) {
//            return
//        }
//
//        _accounts.removeIf { it.id == id }
//
//        viewModelScope.launch {
//            if (id == settings.selectedAccountID) {
//                settings.clearAccountID()
//            }
//        }
//    }
}
