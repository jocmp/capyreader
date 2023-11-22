package com.jocmp.basil

import android.content.Context
import java.io.File
import java.io.FileFilter
import java.util.UUID

class AccountManager(context: Context) {
    var accounts: MutableList<Account> = mutableListOf()
        private set

    private val accountFolder = File(context.filesDir, directoryName)

    init {
        accountFolder.list()?.forEach {
            accounts.add(Account(it))
        }
    }

    fun createAccount(): Account {
        val accountID = UUID.randomUUID().toString()

        if (!accountFolder.exists()) {
            accountFolder.mkdir()
        }

        accountFile(accountID).apply {
            mkdir()
        }

        val account = Account(id = accountID)
        accounts.add(account)
        return account
    }

    fun removeAccount(account: Account) {
        val files = accountFolder.listFiles(AccountFileFilter(account.id))

        val success = files?.first()?.deleteRecursively() ?: false

        if (success) {
            accounts.removeIf { it.id == account.id }
        }
    }

    fun clearAll() {
        accountFolder.deleteRecursively()
    }

    private fun accountFile(id: String): File {
        return File(accountFolder, id)
    }

    companion object {
        private const val directoryName = "accounts"
    }
}

private class AccountFileFilter(private val id: String): FileFilter {
    override fun accept(pathname: File?) = pathname?.name == id
}
