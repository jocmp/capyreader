package com.jocmp.basilreader.ui.accounts

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager

class AccountsViewModel(private val accountManager: AccountManager) : ViewModel() {
    val accounts = accountManager.accounts.toMutableStateList()

    fun createAccount() {
        accounts.add(accountManager.createAccount())
    }

    fun removeAccount(account: Account) {
        accountManager.removeAccount(account)
        accounts.remove(account)
    }
}
