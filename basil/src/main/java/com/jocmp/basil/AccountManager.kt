package com.jocmp.basil

import android.content.Context
import java.io.File

class AccountManager(context: Context) {
    var accounts: MutableList<Account> = mutableListOf()
        private set

    init {
        val accountsFolder = File(context.filesDir, directoryName).apply {
            mkdir()
        }

        val accountID = "fake-777"

        val accountFolder = File(accountsFolder, accountID).apply {
            mkdir()
        }

        Account(context, id = accountID)
    }

    companion object {
        private const val directoryName = "accounts"
    }
}
